package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dao.CuestionarioRepository;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.RespuestaCuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.GuardarCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.GuardarRespuestasDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioRespuestaResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.OpcionCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.PreguntaRespuestaCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.PuntajeCompetenciaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.RespuestaCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dao.ResultadoCompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.model.ResultadoCompetencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dao.PreguntaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CuestionarioRespuestaService {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CuestionarioRepository cuestionarioDAO;
    private final PuestoDAO puestoDAO;
    private final PreguntaDAO preguntaDAO;
    private final RespuestaCuestionarioDAO respuestaDAO;
    private final ResultadoCompetenciaDAO resultadoDAO;

    public CuestionarioRespuestaService(CuestionarioRepository cuestionarioDAO, PuestoDAO puestoDAO,
            PreguntaDAO preguntaDAO, RespuestaCuestionarioDAO respuestaDAO,
            ResultadoCompetenciaDAO resultadoDAO) {
        this.cuestionarioDAO = cuestionarioDAO;
        this.puestoDAO = puestoDAO;
        this.preguntaDAO = preguntaDAO;
        this.respuestaDAO = respuestaDAO;
        this.resultadoDAO = resultadoDAO;
    }

    @Transactional(readOnly = true)
    public CuestionarioRespuestaResumenDTO obtenerResumen(Long cuestionarioId) {
        Cuestionario cuestionario = obtenerCuestionario(cuestionarioId);
        if (cuestionario.getPuestoId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El cuestionario no tiene un puesto asociado");
        }
        var puesto = puestoDAO.findById(cuestionario.getPuestoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Puesto", cuestionario.getPuestoId()));
        List<Long> competencias = puesto.getCaracteristicas().stream().map(c -> c.getCompetencia().getId()).toList();
        int total = preguntaDAO.findActivasByCompetenciaIds(competencias).size();
        int respondidas = respuestaDAO.findByCuestionarioId(cuestionarioId).size();
        String estado = switch (cuestionario.getEstado()) {
            case ACTIVO -> "active";
            case EN_PROCESO -> "in-progress";
            case COMPLETO -> "finished";
            default -> "expired";
        };
        LocalDateTime actualizacion = cuestionario.getFechaFinalizacion() != null ? cuestionario.getFechaFinalizacion()
                : cuestionario.getFechaInicio() != null ? cuestionario.getFechaInicio() : cuestionario.getFechaCreacion();
        List<PuntajeCompetenciaDTO> puntajes = resultadoDAO.findByCuestionarioIdIn(List.of(cuestionarioId)).stream()
                .map(r -> new PuntajeCompetenciaDTO(r.getCompetencia().getId(), r.getCompetencia().getNombre(), r.getPuntaje())).toList();
        return new CuestionarioRespuestaResumenDTO(cuestionario.getId(), "Cuestionario de competencias", puesto.getNombre(),
                puesto.getEmpresa(), estado, formatear(cuestionario.getFechaInicio()),
                formatear(cuestionario.getFechaFinalizacion()), formatear(actualizacion), total, respondidas, puntajes);
    }

    @Transactional(readOnly = true)
    public List<PreguntaRespuestaCuestionarioDTO> obtenerPreguntas(Long cuestionarioId) {
        Cuestionario cuestionario = obtenerCuestionario(cuestionarioId);
        validarAsignado(cuestionario);
        var puesto = puestoDAO.findById(cuestionario.getPuestoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Puesto", cuestionario.getPuestoId()));
        List<Long> competencias = puesto.getCaracteristicas().stream()
                .map(c -> c.getCompetencia().getId()).toList();
        List<Pregunta> preguntas = preguntaDAO.findActivasByCompetenciaIds(competencias);
        Map<Long, Long> seleccionadas = respuestaDAO.findByCuestionarioId(cuestionarioId).stream()
                .collect(Collectors.toMap(r -> r.getPregunta().getId(), r -> r.getOpcion().getId()));
        return preguntas.stream().map(p -> new PreguntaRespuestaCuestionarioDTO(p.getId(), p.getEnunciado(),
                p.getFactor().getCompetencia().getNombre(), p.getOpciones().stream()
                .map(o -> new OpcionCuestionarioDTO(o.getId(),
                        o.getDescripcion() == null || o.getDescripcion().isBlank() ? o.getNombre() : o.getDescripcion()))
                .toList(), seleccionadas.get(p.getId()))).toList();
    }

    @Transactional
    public GuardarCuestionarioDTO guardarRespuestas(Long cuestionarioId, GuardarRespuestasDTO request) {
        Cuestionario cuestionario = obtenerCuestionario(cuestionarioId);
        validarEditable(cuestionario);
        var puesto = puestoDAO.findById(cuestionario.getPuestoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Puesto", cuestionario.getPuestoId()));
        Set<Long> competencias = puesto.getCaracteristicas().stream()
                .map(c -> c.getCompetencia().getId()).collect(Collectors.toSet());
        List<Pregunta> preguntas = preguntaDAO.findActivasByCompetenciaIds(competencias);
        Map<Long, Pregunta> preguntasPorId = preguntas.stream().collect(Collectors.toMap(Pregunta::getId, Function.identity()));

        Set<Long> idsEnviados = request.respuestas().stream().map(r -> r.preguntaId()).collect(Collectors.toSet());
        if (idsEnviados.size() != request.respuestas().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede enviar más de una opción para la misma pregunta");
        }

        List<RespuestaCuestionario> guardar = new ArrayList<>();
        for (var seleccion : request.respuestas()) {
            Pregunta pregunta = preguntasPorId.get(seleccion.preguntaId());
            if (pregunta == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La pregunta no pertenece al cuestionario");
            }
            OpcionRespuesta opcion = pregunta.getOpciones().stream()
                    .filter(o -> o.getId().equals(seleccion.opcionId())).findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "La opción seleccionada no pertenece a la pregunta"));
            RespuestaCuestionario respuesta = respuestaDAO.findByCuestionarioIdAndPreguntaId(cuestionarioId, pregunta.getId())
                    .orElseGet(() -> new RespuestaCuestionario(cuestionario, pregunta, opcion));
            respuesta.setOpcion(opcion);
            guardar.add(respuesta);
        }
        respuestaDAO.saveAll(guardar);

        List<RespuestaCuestionario> todas = respuestaDAO.findByCuestionarioId(cuestionarioId);
        boolean completo = !preguntas.isEmpty() && todas.size() == preguntas.size();
        if (!completo) {
            cuestionario.setEstado(EstadoCuestionario.EN_PROCESO);
            cuestionario.setFechaInicio(cuestionario.getFechaInicio() == null ? LocalDateTime.now() : cuestionario.getFechaInicio());
            return new GuardarCuestionarioDTO(cuestionario.getEstado().name(), todas.size(), preguntas.size(), List.of());
        }

        Map<Long, List<RespuestaCuestionario>> porCompetencia = todas.stream().collect(Collectors.groupingBy(
                r -> r.getPregunta().getFactor().getCompetencia().getId()));
        List<PuntajeCompetenciaDTO> puntajes = new ArrayList<>();
        for (CaracteristicaPuesto caracteristica : puesto.getCaracteristicas()) {
            var competencia = caracteristica.getCompetencia();
            List<RespuestaCuestionario> respuestasCompetencia = porCompetencia.getOrDefault(competencia.getId(), List.of());
            double totalObtenido = respuestasCompetencia.stream().mapToDouble(r -> r.getOpcion().getPonderacion()).sum();
            double maximo = respuestasCompetencia.stream().mapToDouble(r -> r.getPregunta().getOpciones().stream()
                    .mapToDouble(OpcionRespuesta::getPonderacion).max().orElse(0.0)).sum();
            double puntaje = maximo == 0 ? 0 : redondear(totalObtenido / maximo * 10.0);
            puntajes.add(new PuntajeCompetenciaDTO(competencia.getId(), competencia.getNombre(), puntaje));
            ResultadoCompetencia resultado = resultadoDAO.findByCuestionarioIdAndCompetenciaId(cuestionarioId, competencia.getId())
                    .orElseGet(() -> new ResultadoCompetencia(cuestionario, competencia, puntaje));
            resultado.setPuntaje(puntaje);
            resultadoDAO.save(resultado);
        }
        cuestionario.setEstado(EstadoCuestionario.COMPLETO);
        cuestionario.setFechaInicio(cuestionario.getFechaInicio() == null ? LocalDateTime.now() : cuestionario.getFechaInicio());
        cuestionario.setFechaFinalizacion(LocalDateTime.now());
        return new GuardarCuestionarioDTO(cuestionario.getEstado().name(), todas.size(), preguntas.size(), puntajes);
    }

    private Cuestionario obtenerCuestionario(Long id) {
        return cuestionarioDAO.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuestionario", id));
    }

    private void validarEditable(Cuestionario cuestionario) {
        validarAsignado(cuestionario);
        if (cuestionario.getEstado() == EstadoCuestionario.COMPLETO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El cuestionario ya fue completado");
        }
    }

    private void validarAsignado(Cuestionario cuestionario) {
        if (cuestionario.getPuestoId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El cuestionario no tiene un puesto asociado");
        }
    }

    private String formatear(LocalDateTime fecha) { return fecha == null ? null : FECHA_HORA.format(fecha); }

    private double redondear(double valor) { return Math.round(valor * 100.0) / 100.0; }
}
