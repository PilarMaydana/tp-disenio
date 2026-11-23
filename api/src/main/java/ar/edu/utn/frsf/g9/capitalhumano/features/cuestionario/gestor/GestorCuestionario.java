package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.ConfiguracionDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioWizardDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.InstruccionesDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.OpcionCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.PreguntaCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.RespuestaBloqueDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.exception.CuestionarioNoDisponibleException;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.BloqueCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Configuracion;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.PreguntaAsignada;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.RespuestaCandidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Resultado;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.ResultadoCompetencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.ResultadoFactor;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao.EvaluacionDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao.FactorDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.dao.PreguntaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Gestor de lógica de negocio de los Cuestionarios.
 *
 * <ul>
 *   <li>CU 025: genera la {@link Evaluacion} con un cuestionario ACTIVO por candidato.</li>
 *   <li>CU 026: el candidato completa su cuestionario por bloques y se calculan los resultados.</li>
 * </ul>
 *
 * <p>Los nombres de los métodos siguen el diagrama de secuencia del CU 026.</p>
 */
@Service
public class GestorCuestionario {

    private static final Logger log = LoggerFactory.getLogger(GestorCuestionario.class);

    private final EvaluacionDAO evaluacionDAO;
    private final CuestionarioDAO cuestionarioDAO;
    private final ConfiguracionDAO configuracionDAO;
    private final PreguntaDAO preguntaDAO;
    private final FactorDAO factorDAO;
    private final PuestoDAO puestoDAO;

    public GestorCuestionario(EvaluacionDAO evaluacionDAO,
                              CuestionarioDAO cuestionarioDAO,
                              ConfiguracionDAO configuracionDAO,
                              PreguntaDAO preguntaDAO,
                              FactorDAO factorDAO,
                              PuestoDAO puestoDAO) {
        this.evaluacionDAO = evaluacionDAO;
        this.cuestionarioDAO = cuestionarioDAO;
        this.configuracionDAO = configuracionDAO;
        this.preguntaDAO = preguntaDAO;
        this.factorDAO = factorDAO;
        this.puestoDAO = puestoDAO;
    }

    // =====================================================================
    // CU 025
    // =====================================================================

    /**
     * Genera y persiste una evaluación para el puesto con un cuestionario ACTIVO por candidato
     * (CU 025 - mensajes 57 a 60).
     *
     * <p>Todos los cuestionarios de la evaluación referencian al mismo puesto, por lo que se basan
     * en el mismo modelo de competencias, factores, preguntas y opciones de respuesta. Cada
     * cuestionario guarda una copia de los parámetros vigentes de la configuración.</p>
     *
     * @param candidatosConClave candidatos a evaluar con la clave de acceso asignada a cada uno
     *                           (se respeta el orden de inserción)
     * @param puesto             puesto o función para el cual se evalúa
     * @param usuarioId          consultor que genera la evaluación
     * @return evaluación guardada con sus cuestionarios
     */
    @Transactional
    public Evaluacion generarCuestionariosParaEvaluacion(Map<Candidato, String> candidatosConClave,
                                                         Puesto puesto,
                                                         Long usuarioId) {
        LocalDateTime ahora = LocalDateTime.now();
        Configuracion configuracion = configuracionDAO.obtener();
        Evaluacion evaluacion = new Evaluacion(puesto, ahora, usuarioId);

        candidatosConClave.forEach((candidato, clave) -> {
            Cuestionario cuestionario = new Cuestionario();
            cuestionario.setCandidato(candidato);
            cuestionario.setPuestoId(puesto.getId());
            cuestionario.setEstado(EstadoCuestionario.ACTIVO);
            cuestionario.setClaveAcceso(clave);
            cuestionario.setFechaCreacion(ahora);
            cuestionario.setFechaCambioEstado(ahora);
            cuestionario.setEliminado(false);
            cuestionario.aplicarConfiguracion(configuracion);
            evaluacion.agregarCuestionario(cuestionario);
        });

        Evaluacion guardada = evaluacionDAO.save(evaluacion);
        log.info("Evaluación {} creada para el puesto {} con {} cuestionario(s)",
                guardada.getId(), puesto.getCodigo(), guardada.getCuestionarios().size());
        return guardada;
    }

    // =====================================================================
    // CU 026 - pasos 1 a 4
    // =====================================================================

    /**
     * Devuelve las instrucciones del cuestionario (CU 026 - mensajes 2 y 3).
     *
     * @return instrucciones y parámetros
     */
    @Transactional(readOnly = true)
    public InstruccionesDTO obtenerInstrucciones() {
        Configuracion c = configuracionDAO.obtener();
        return new InstruccionesDTO(c.getInstrucciones(), c.getTiempoMaximoMinutos(),
                c.getCantidadPreguntasPorBloque(), c.getCantidadMaximaAccesos());
    }

    /**
     * Datos del cuestionario abierto del candidato, para la pantalla de ingreso.
     *
     * @param nroCandidato número de candidato autenticado
     * @return resumen del cuestionario
     */
    @Transactional(readOnly = true)
    public CuestionarioResumenDTO obtenerResumen(Long nroCandidato) {
        Cuestionario c = obtenerAbierto(nroCandidato);
        Puesto puesto = obtenerPuesto(c);
        return new CuestionarioResumenDTO(c.getId(), c.getEstado(), puesto.getNombre(), puesto.getEmpresa(),
                c.getFechaCreacion(), c.getFechaInicio(), c.getFechaUltimoIngreso(), c.getCantidadAccesos(),
                c.getCantidadMaximaAccesos(), c.getTiempoMaximoMinutos(), c.getCantidadPreguntas(),
                c.getCantidadRespondidas(), c.segundosRestantes(LocalDateTime.now()));
    }

    /**
     * El candidato acepta las instrucciones: se inicia el cuestionario (primer ingreso) o se
     * reanuda en el bloque pendiente (1.C) (CU 026 - mensajes 8 a 44).
     *
     * <p>Si se superó el tiempo máximo o la cantidad de accesos (1.A) el cuestionario pasa a
     * INCOMPLETO; si estuvo ACTIVO más tiempo del permitido (1.B) pasa a SIN_CONTESTAR. En ambos
     * casos el cambio de estado se guarda y se lanza {@link CuestionarioNoDisponibleException}.</p>
     *
     * @param nroCandidato número de candidato autenticado
     * @return primer bloque sin responder
     */
    @Transactional(noRollbackFor = CuestionarioNoDisponibleException.class)
    public CuestionarioWizardDTO iniciarOReanudarCuestionario(Long nroCandidato) {
        Cuestionario cuestionario = obtenerAbierto(nroCandidato);
        LocalDateTime ahora = LocalDateTime.now();
        completarParametros(cuestionario);

        verificarVigencia(cuestionario, ahora, true);

        boolean primerIngreso = cuestionario.getEstado() == EstadoCuestionario.ACTIVO
                || cuestionario.getBloques().isEmpty();
        cuestionario.registrarAcceso(ahora);

        if (primerIngreso) {
            // Paso 3: preguntas al azar, 2 por factor, mezcladas y agrupadas en bloques
            List<Pregunta> preguntas = seleccionarPreguntas(obtenerPuesto(cuestionario));
            if (preguntas.isEmpty()) {
                throw new ReglaNegocioException(
                        "El puesto no posee competencias con factores que tengan al menos 2 preguntas");
            }
            cuestionario.armarBloques(preguntas);
            if (cuestionario.getFechaInicio() == null) {
                cuestionario.setFechaInicio(ahora);
            }
            cuestionario.cambiarEstado(EstadoCuestionario.EN_PROCESO, ahora);
            log.info("Cuestionario {} iniciado: {} preguntas en {} bloque(s)", cuestionario.getId(),
                    preguntas.size(), cuestionario.getBloques().size());
        } else {
            log.info("Cuestionario {} reanudado (ingreso {})", cuestionario.getId(), cuestionario.getCantidadAccesos());
        }

        Cuestionario guardado = cuestionarioDAO.save(cuestionario);
        return armarWizardDTO(guardado, ahora);
    }

    /**
     * Elige las preguntas del cuestionario: por cada competencia del puesto, por cada factor con
     * al menos 2 preguntas, toma 2 al azar; luego mezcla todas (CU 026 - mensajes 32 a 36).
     *
     * @param puesto puesto evaluado
     * @return preguntas en el orden en que se presentarán
     */
    public List<Pregunta> seleccionarPreguntas(Puesto puesto) {
        List<Pregunta> seleccionadas = new ArrayList<>();
        for (CaracteristicaPuesto caracteristica : puesto.getCaracteristicas()) {
            List<Factor> factores = factorDAO
                    .findByCompetenciaIdOrderByNroOrdenAsc(caracteristica.getCompetencia().getId());
            for (Factor factor : factores) {
                if (!Boolean.TRUE.equals(factor.getEliminado()) && factor.tieneMinimoDosPreguntas()) {
                    seleccionadas.addAll(preguntaDAO.obtenerAleatoriasPorFactor(
                            factor.getCodigo(), ResultadoFactor.PREGUNTAS_POR_FACTOR));
                }
            }
        }
        Collections.shuffle(seleccionadas);
        return seleccionadas;
    }

    // =====================================================================
    // CU 026 - pasos 5 a 8
    // =====================================================================

    /**
     * Guarda las respuestas de un bloque (CU 026 - mensajes 47 a 72).
     *
     * <p>Valida que se hayan respondido todas las preguntas del bloque (6.A). Si era el último
     * bloque calcula los resultados y el cuestionario pasa a COMPLETO (paso 8); si no, devuelve
     * el bloque siguiente (8.A).</p>
     *
     * @param nroCandidato   número de candidato autenticado
     * @param cuestionarioId cuestionario que se responde
     * @param numeroBloque   bloque que se responde
     * @param respuestas     opción elegida para cada pregunta del bloque
     * @return siguiente bloque, o el cuestionario completado
     */
    @Transactional(noRollbackFor = CuestionarioNoDisponibleException.class)
    public CuestionarioWizardDTO responderBloque(Long nroCandidato, Long cuestionarioId, Integer numeroBloque,
                                                 List<RespuestaBloqueDTO> respuestas) {
        Cuestionario cuestionario = cuestionarioDAO.findById(cuestionarioId)
                .filter(c -> c.getCandidato().getNroCandidato().equals(nroCandidato))
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuestionario", cuestionarioId));
        if (cuestionario.getEstado() != EstadoCuestionario.EN_PROCESO) {
            throw new ReglaNegocioException("El cuestionario no está en proceso");
        }
        LocalDateTime ahora = LocalDateTime.now();
        completarParametros(cuestionario);
        verificarVigencia(cuestionario, ahora, false);

        BloqueCuestionario bloque = cuestionario.getBloquePendiente();
        if (bloque == null || !bloque.getNumero().equals(numeroBloque)) {
            throw new ReglaNegocioException("El bloque " + numeroBloque
                    + " ya fue respondido o no corresponde. No es posible volver a un bloque anterior.");
        }

        // 6.A - todas las preguntas del bloque deben estar respondidas con una opción válida
        Map<Long, Long> opcionPorPregunta = new HashMap<>();
        if (respuestas != null) {
            respuestas.stream()
                    .filter(r -> r.preguntaAsignadaId() != null && r.opcionId() != null)
                    .forEach(r -> opcionPorPregunta.put(r.preguntaAsignadaId(), r.opcionId()));
        }
        Map<PreguntaAsignada, OpcionRespuesta> elegidas = new LinkedHashMap<>();
        List<Integer> sinResponder = new ArrayList<>();
        for (PreguntaAsignada asignada : bloque.getPreguntas()) {
            Long opcionId = opcionPorPregunta.get(asignada.getId());
            OpcionRespuesta opcion = opcionId == null ? null : asignada.getPregunta().getOpciones().stream()
                    .filter(o -> o.getId().equals(opcionId))
                    .findFirst()
                    .orElse(null);
            if (opcion == null) {
                sinResponder.add(asignada.getNroOrden());
            } else {
                elegidas.put(asignada, opcion);
            }
        }
        if (!sinResponder.isEmpty()) {
            throw new ReglaNegocioException("Existen preguntas sin responder: "
                    + sinResponder.stream().map(n -> "Pregunta " + n).collect(Collectors.joining(", ")));
        }

        bloque.responder(elegidas, ahora);

        if (cuestionario.getBloquePendiente() == null) {
            // Paso 8 - último bloque
            calcularResultados(cuestionario, ahora);
            cuestionario.setFechaFinalizacion(ahora);
            cuestionario.cambiarEstado(EstadoCuestionario.COMPLETO, ahora);
            log.info("Cuestionario {} completado. Puntaje: {}", cuestionario.getId(),
                    cuestionario.getResultado().getPuntajeObtenido());
        }

        Cuestionario guardado = cuestionarioDAO.save(cuestionario);
        return armarWizardDTO(guardado, ahora);
    }

    /**
     * Calcula los resultados del cuestionario (observación 3 del CU 026 - mensajes 57 a 63):
     * <pre>
     * por cada competencia del puesto
     *     por cada factor evaluado: total = suma de ponderaciones de las opciones elegidas / 2
     *     total competencia = suma de totales de los factores / cantidad de factores evaluados
     * puntaje obtenido = promedio de los totales de las competencias evaluadas
     * </pre>
     *
     * @param cuestionario cuestionario con todos sus bloques respondidos
     * @param ahora        fecha y hora del cálculo
     */
    void calcularResultados(Cuestionario cuestionario, LocalDateTime ahora) {
        Map<Factor, List<RespuestaCandidato>> respuestasPorFactor = new LinkedHashMap<>();
        cuestionario.getBloques().stream()
                .flatMap(b -> b.getPreguntas().stream())
                .sorted(Comparator.comparing(PreguntaAsignada::getNroOrden))
                .filter(PreguntaAsignada::fueRespondida)
                .forEach(pa -> respuestasPorFactor
                        .computeIfAbsent(pa.getPregunta().getFactor(), f -> new ArrayList<>())
                        .add(pa.getRespuesta()));

        Resultado resultado = new Resultado(ahora);
        for (CaracteristicaPuesto caracteristica : obtenerPuesto(cuestionario).getCaracteristicas()) {
            Long competenciaId = caracteristica.getCompetencia().getId();
            List<Factor> factoresEvaluados = respuestasPorFactor.keySet().stream()
                    .filter(f -> Objects.equals(f.getCompetencia().getId(), competenciaId))
                    .sorted(Comparator.comparing(Factor::getNroOrden))
                    .toList();
            if (factoresEvaluados.isEmpty()) {
                continue; // competencia que no pudo evaluarse
            }
            ResultadoCompetencia rc = new ResultadoCompetencia(caracteristica);
            for (Factor factor : factoresEvaluados) {
                ResultadoFactor rf = new ResultadoFactor(factor);
                rf.calcularTotal(respuestasPorFactor.get(factor));
                rc.agregarResultadoFactor(rf);
            }
            rc.calcularTotal();
            resultado.agregarResultadoCompetencia(rc);
        }
        cuestionario.setResultado(resultado);
    }

    // =====================================================================
    // Auxiliares
    // =====================================================================

    private Cuestionario obtenerAbierto(Long nroCandidato) {
        return cuestionarioDAO.obtenerActivoOEnProceso(nroCandidato)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No posee un cuestionario activo o en proceso"));
    }

    /**
     * Cuestionarios creados antes de existir la configuración no tienen los parámetros copiados:
     * se completan con los vigentes.
     */
    private void completarParametros(Cuestionario c) {
        if (c.getTiempoMaximoMinutos() == null || c.getCantidadPreguntasPorBloque() == null
                || c.getTiempoMaximoActivoHoras() == null || c.getCantidadMaximaAccesos() == null) {
            c.aplicarConfiguracion(configuracionDAO.obtener());
        }
    }

    /**
     * Flujos 1.A y 1.B: si el cuestionario venció, cambia su estado y lanza la excepción.
     *
     * @param controlarAccesos si se debe controlar la cantidad máxima de accesos (al ingresar)
     */
    private void verificarVigencia(Cuestionario c, LocalDateTime ahora, boolean controlarAccesos) {
        if (c.getEstado() == EstadoCuestionario.EN_PROCESO
                && (c.superaTiempoMaximo(ahora) || (controlarAccesos && c.superaMaximoAccesos()))) {
            c.cambiarEstado(EstadoCuestionario.INCOMPLETO, ahora);
            cuestionarioDAO.save(c);
            String motivo = c.superaTiempoMaximo(ahora)
                    ? "Se superó el tiempo máximo de " + c.getTiempoMaximoMinutos() + " minutos para completar el cuestionario."
                    : "Se superó la cantidad máxima de " + c.getCantidadMaximaAccesos() + " accesos al cuestionario.";
            throw new CuestionarioNoDisponibleException(CuestionarioNoDisponibleException.INCOMPLETO,
                    motivo + " El cuestionario quedó en estado incompleto.");
        }
        if (c.getEstado() == EstadoCuestionario.ACTIVO && c.superaTiempoMaximoActivo(ahora)) {
            c.cambiarEstado(EstadoCuestionario.SIN_CONTESTAR, ahora);
            cuestionarioDAO.save(c);
            throw new CuestionarioNoDisponibleException(CuestionarioNoDisponibleException.SIN_CONTESTAR,
                    "El plazo para comenzar el cuestionario venció. El cuestionario quedó en estado sin contestar.");
        }
    }

    private Puesto obtenerPuesto(Cuestionario c) {
        if (c.getEvaluacion() != null) {
            return c.getEvaluacion().getPuesto();
        }
        if (c.getPuestoId() != null) {
            return puestoDAO.findById(c.getPuestoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Puesto", c.getPuestoId()));
        }
        throw new ReglaNegocioException("El cuestionario no tiene un puesto asociado");
    }

    /**
     * Arma el paso del wizard posicionado en el bloque pendiente (mensajes 27, 43 y 70).
     */
    private CuestionarioWizardDTO armarWizardDTO(Cuestionario c, LocalDateTime ahora) {
        Puesto puesto = obtenerPuesto(c);
        BloqueCuestionario pendiente = c.getBloquePendiente();
        List<PreguntaCuestionarioDTO> preguntas = pendiente == null ? List.of() : pendiente.getPreguntas().stream()
                .sorted(Comparator.comparing(PreguntaAsignada::getNroOrden))
                .map(pa -> new PreguntaCuestionarioDTO(pa.getId(), pa.getNroOrden(), pa.getPregunta().getEnunciado(),
                        pa.getPregunta().getOpciones().stream()
                                .sorted(Comparator.comparing(OpcionRespuesta::getNroOrden,
                                        Comparator.nullsLast(Comparator.naturalOrder())))
                                .map(o -> new OpcionCuestionarioDTO(o.getId(), textoOpcion(o)))
                                .toList()))
                .toList();
        return new CuestionarioWizardDTO(c.getId(), puesto.getNombre(), puesto.getEmpresa(), c.getEstado(),
                pendiente == null ? null : pendiente.getNumero(), c.getBloques().size(), c.getCantidadPreguntas(),
                c.getCantidadRespondidas(), c.segundosRestantes(ahora), pendiente == null, preguntas);
    }

    private static String textoOpcion(OpcionRespuesta o) {
        return o.getDescripcion() != null && !o.getDescripcion().isBlank() ? o.getDescripcion() : o.getNombre();
    }
}
