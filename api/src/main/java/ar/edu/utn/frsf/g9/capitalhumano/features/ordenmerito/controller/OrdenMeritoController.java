package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dao.CuestionarioOrdenMeritoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dao.ResultadoCompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.CandidatoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.EvaluacionOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.PuestoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.model.ResultadoCompetencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Consultas de puestos, cuestionarios y resultados para el CU 028. */
@RestController
@RequestMapping("/api/ordenes-merito")
@Tag(name = "Orden de mérito", description = "Consultas para emitir la orden de mérito (CU 028)")
public class OrdenMeritoController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es-AR"));
    private final PuestoDAO puestoDAO;
    private final CuestionarioOrdenMeritoDAO cuestionarioDAO;
    private final ResultadoCompetenciaDAO resultadoDAO;

    public OrdenMeritoController(PuestoDAO puestoDAO, CuestionarioOrdenMeritoDAO cuestionarioDAO,
                                 ResultadoCompetenciaDAO resultadoDAO) {
        this.puestoDAO = puestoDAO;
        this.cuestionarioDAO = cuestionarioDAO;
        this.resultadoDAO = resultadoDAO;
    }

    @GetMapping("/puestos")
    @Transactional(readOnly = true)
    @Operation(summary = "Buscar puestos para emitir orden de mérito")
    public List<PuestoOrdenMeritoDTO> buscarPuestos(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombrePuesto,
            @RequestParam(required = false) String empresa) {
        return puestoDAO.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getEliminado()))
                .filter(p -> contiene(p.getCodigo(), codigo))
                .filter(p -> contiene(p.getNombre(), nombrePuesto))
                .filter(p -> contiene(p.getEmpresa(), empresa))
                .map(p -> {
                    List<Cuestionario> cuestionarios = cuestionarioDAO.findByPuestoIdAndEliminadoFalseOrderByIdAsc(p.getId());
                    return new PuestoOrdenMeritoDTO(p.getId(), p.getCodigo(), p.getNombre(), p.getEmpresa(), p.getEmpresa(),
                            p.getDescripcion(), cuestionarios.size(), cuestionarios.stream()
                            .filter(c -> c.getEstado() != null && c.getEstado().name().equals("COMPLETO")).count());
                }).toList();
    }

    @GetMapping("/empresas")
    @Transactional(readOnly = true)
    @Operation(summary = "Listar empresas con puestos registrados")
    public List<String> listarEmpresas() {
        return puestoDAO.findAll().stream().filter(p -> !Boolean.TRUE.equals(p.getEliminado()))
                .map(Puesto::getEmpresa).distinct().sorted().toList();
    }

    @GetMapping("/puestos/{puestoId}/evaluaciones")
    @Transactional(readOnly = true)
    @Operation(summary = "Listar cuestionarios asociados al puesto")
    public List<EvaluacionOrdenMeritoDTO> listarEvaluaciones(@PathVariable Long puestoId) {
        validarPuesto(puestoId);
        return cuestionarioDAO.findByPuestoIdAndEliminadoFalseOrderByIdAsc(puestoId).stream()
                .map(c -> new EvaluacionOrdenMeritoDTO(c.getId(), fecha(c.getFechaInicio() != null ? c.getFechaInicio() : c.getFechaCreacion()),
                        "Cuestionario de " + c.getCandidato().getApellido() + ", " + c.getCandidato().getNombre(),
                        c.getEstado() != null && c.getEstado().name().equals("COMPLETO") ? "Completada" : "Parcial"))
                .toList();
    }

    @GetMapping("/puestos/{puestoId}/reporte")
    @Transactional(readOnly = true)
    @Operation(summary = "Obtener candidatos de las evaluaciones seleccionadas")
    public List<CandidatoOrdenMeritoDTO> obtenerReporte(@PathVariable Long puestoId,
            @RequestParam List<Long> evaluacionIds) {
        Puesto puesto = validarPuesto(puestoId);
        List<Cuestionario> cuestionarios = cuestionarioDAO.findByPuestoIdAndEliminadoFalseOrderByIdAsc(puestoId).stream()
                .filter(c -> evaluacionIds.contains(c.getId())).toList();
        Set<Long> idsEncontrados = cuestionarios.stream().map(Cuestionario::getId).collect(Collectors.toSet());
        if (idsEncontrados.size() != evaluacionIds.stream().distinct().count()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una o más evaluaciones no pertenecen al puesto seleccionado");
        }

        Map<Long, Map<Long, Double>> puntajes = resultadoDAO.findByCuestionarioIdIn(cuestionarios.stream()
                        .map(Cuestionario::getId).toList()).stream()
                .collect(Collectors.groupingBy(r -> r.getCuestionario().getId(),
                        Collectors.toMap(r -> r.getCompetencia().getId(), ResultadoCompetencia::getPuntaje)));

        return cuestionarios.stream().map(c -> armarResultado(c, puesto, puntajes.getOrDefault(c.getId(), Map.of())))
                .sorted((a, b) -> {
                    int grupo = Integer.compare(prioridad(a.estadoMerito()), prioridad(b.estadoMerito()));
                    if (grupo != 0) return grupo;
                    if (a.puntajeObtenido() == null) return b.puntajeObtenido() == null ? 0 : 1;
                    if (b.puntajeObtenido() == null) return -1;
                    return Double.compare(b.puntajeObtenido(), a.puntajeObtenido());
                }).toList();
    }

    private CandidatoOrdenMeritoDTO armarResultado(Cuestionario cuestionario, Puesto puesto, Map<Long, Double> puntajes) {
        boolean completo = cuestionario.getEstado() != null && cuestionario.getEstado().name().equals("COMPLETO");
        boolean tieneTodosLosPuntajes = !puesto.getCaracteristicas().isEmpty() && puesto.getCaracteristicas().stream()
                .allMatch(c -> puntajes.containsKey(c.getCompetencia().getId()));
        Double total = tieneTodosLosPuntajes ? puesto.getCaracteristicas().stream()
                .mapToDouble(c -> puntajes.get(c.getCompetencia().getId())).sum() : null;
        Boolean cumple = null;
        String estadoMerito;
        if (!completo) estadoMerito = "CUESTIONARIO_INCOMPLETO";
        else if (!tieneTodosLosPuntajes) estadoMerito = "PENDIENTE_PUNTAJE";
        else {
            cumple = puesto.getCaracteristicas().stream().allMatch(c ->
                    puntajes.get(c.getCompetencia().getId()) >= c.getPonderacionRequerida());
            estadoMerito = cumple ? "APTO" : "NO_ALCANZA_MINIMOS";
        }
        return new CandidatoOrdenMeritoDTO(cuestionario.getId(),
                cuestionario.getCandidato().getTipoDocumento().name(), cuestionario.getCandidato().getNroDocumento(),
                cuestionario.getCandidato().getApellido(), cuestionario.getCandidato().getNombre(),
                cuestionario.getEstado() == null ? "SIN_ESTADO" : cuestionario.getEstado().name(), total,
                estadoMerito, cumple, cuestionario.getFechaInicio(), cuestionario.getFechaFinalizacion(),
                cuestionario.getCandidato().getEscolaridad());
    }

    private int prioridad(String estado) {
        return switch (estado) {
            case "APTO" -> 0;
            case "NO_ALCANZA_MINIMOS" -> 1;
            case "PENDIENTE_PUNTAJE" -> 2;
            default -> 3;
        };
    }

    private Puesto validarPuesto(Long puestoId) {
        return puestoDAO.findById(puestoId).filter(p -> !Boolean.TRUE.equals(p.getEliminado()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Puesto", puestoId));
    }

    private boolean contiene(String valor, String criterio) {
        return criterio == null || criterio.isBlank() || (valor != null && valor.toLowerCase(Locale.ROOT).contains(criterio.trim().toLowerCase(Locale.ROOT)));
    }

    private String fecha(LocalDateTime fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }
}
