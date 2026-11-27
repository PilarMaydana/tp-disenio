package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioWizardDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.PreguntaCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.RespuestaBloqueDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.exception.CuestionarioNoDisponibleException;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GestorCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.ResultadoCompetencia;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.PreguntaAsignada;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.OpcionRespuesta;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integración del CU 026 - Completar cuestionario, contra la base H2 cargada con
 * {@code seed.sql} (candidatos 15 a 18 preparados para cada flujo).
 *
 * <p>Los vencimientos (1.A y 1.B) se simulan dentro de cada test moviendo las fechas del
 * cuestionario, porque en el seed los cuestionarios vencidos ya están cerrados.</p>
 */
@SpringBootTest
@Transactional
class CompletarCuestionarioIntegrationTest {

    private static final long SUAREZ_ACTIVO = 16L;
    private static final long TORRES_EN_PROCESO = 15L;
    /** En el seed ya figuran cerrados por vencimiento (los cerró el proceso automático). */
    private static final long MEDINA_INCOMPLETO = 17L;
    private static final long HERRERA_SIN_CONTESTAR = 18L;

    @Autowired
    private GestorCuestionario gestor;

    @Autowired
    private CuestionarioDAO cuestionarioDAO;

    /** Busca un cuestionario del seed por su clave (los ids dependen del orden de carga del seed). */
    private Cuestionario porClave(String clave) {
        return cuestionarioDAO.findAll().stream()
                .filter(c -> clave.equals(c.getClaveAcceso()))
                .findFirst()
                .orElseThrow();
    }

    /** Elige la opción de mayor texto alfabético "SIEMPRE"/"UN PARADIGMA..." no importa: toma la primera. */
    private static List<RespuestaBloqueDTO> responderPrimeraOpcion(CuestionarioWizardDTO paso) {
        return paso.preguntas().stream()
                .map(p -> new RespuestaBloqueDTO(p.preguntaAsignadaId(), p.opciones().get(0).id()))
                .toList();
    }

    @Test
    @DisplayName("Instrucciones y parámetros se toman de la configuración")
    void instrucciones() {
        var instrucciones = gestor.obtenerInstrucciones();
        assertThat(instrucciones.instrucciones()).contains("BLOQUES");
        assertThat(instrucciones.tiempoMaximoMinutos()).isEqualTo(60);
        assertThat(instrucciones.cantidadPreguntasPorBloque()).isEqualTo(2);
    }

    @Test
    @DisplayName("Primer ingreso: 2 preguntas por factor, en bloques de 2, y el cuestionario pasa a EN_PROCESO")
    void primerIngreso() {
        CuestionarioWizardDTO paso = gestor.iniciarOReanudarCuestionario(SUAREZ_ACTIVO);

        // BACK-01: TEC-01 (FAC-03, 2 preguntas) + COMUNICACIÓN (FAC-01, 2 preguntas) = 4 preguntas
        assertThat(paso.totalPreguntas()).isEqualTo(4);
        assertThat(paso.totalBloques()).isEqualTo(2);
        assertThat(paso.numeroBloque()).isEqualTo(1);
        assertThat(paso.preguntas()).hasSize(2)
                .extracting(PreguntaCuestionarioDTO::nroOrden).containsExactly(1, 2);
        assertThat(paso.estado()).isEqualTo(EstadoCuestionario.EN_PROCESO);
        assertThat(paso.segundosRestantes()).isBetween(3500L, 3600L);
        assertThat(paso.preguntas().get(0).opciones()).isNotEmpty();

        Cuestionario c = cuestionarioDAO.findById(paso.cuestionarioId()).orElseThrow();
        assertThat(c.getFechaInicio()).isNotNull();
        assertThat(c.getCantidadAccesos()).isEqualTo(1);
    }

    @Test
    @DisplayName("6.A: una opción que pertenece a otra pregunta se rechaza como no respondida")
    void opcionDeOtraPregunta() {
        CuestionarioWizardDTO paso = gestor.iniciarOReanudarCuestionario(SUAREZ_ACTIVO);
        PreguntaCuestionarioDTO primera = paso.preguntas().get(0);
        PreguntaCuestionarioDTO segunda = paso.preguntas().get(1);
        Long opcionDeLaSegunda = segunda.opciones().get(0).id();

        // A la pregunta 1 se le envía una opción válida pero de la pregunta 2 (respuesta adulterada)
        List<RespuestaBloqueDTO> adulterada = List.of(
                new RespuestaBloqueDTO(primera.preguntaAsignadaId(), opcionDeLaSegunda),
                new RespuestaBloqueDTO(segunda.preguntaAsignadaId(), opcionDeLaSegunda));

        assertThatThrownBy(() -> gestor.responderBloque(SUAREZ_ACTIVO, paso.cuestionarioId(), 1, adulterada))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Pregunta 1")
                .hasMessageNotContaining("Pregunta 2");

        // El bloque no se guardó: sigue pendiente el bloque 1
        assertThat(gestor.iniciarOReanudarCuestionario(SUAREZ_ACTIVO).numeroBloque()).isEqualTo(1);
    }

    @Test
    @DisplayName("6.A: si faltan respuestas se informa cuáles y no se guarda el bloque")
    void preguntasSinResponder() {
        CuestionarioWizardDTO paso = gestor.iniciarOReanudarCuestionario(SUAREZ_ACTIVO);
        PreguntaCuestionarioDTO primera = paso.preguntas().get(0);

        List<RespuestaBloqueDTO> soloUna = List.of(
                new RespuestaBloqueDTO(primera.preguntaAsignadaId(), primera.opciones().get(0).id()));

        assertThatThrownBy(() -> gestor.responderBloque(SUAREZ_ACTIVO, paso.cuestionarioId(), 1, soloUna))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Pregunta 2")
                .hasMessageNotContaining("Pregunta 1");
    }

    @Test
    @DisplayName("Flujo completo: responde los 2 bloques, calcula resultados y pasa a COMPLETO")
    void flujoCompletoConResultados() {
        CuestionarioWizardDTO paso1 = gestor.iniciarOReanudarCuestionario(SUAREZ_ACTIVO);
        // Las preguntas se eligen al azar: el puntaje esperado se calcula con las que tocaron
        double esperado = puntajeConMejoresOpciones(paso1.cuestionarioId());

        // 8.A: hay más bloques
        CuestionarioWizardDTO paso2 = gestor.responderBloque(SUAREZ_ACTIVO, paso1.cuestionarioId(), 1,
                respuestasMaximas(paso1));
        assertThat(paso2.completado()).isFalse();
        assertThat(paso2.numeroBloque()).isEqualTo(2);
        assertThat(paso2.preguntasRespondidas()).isEqualTo(2);

        // No se puede volver a responder el bloque 1
        assertThatThrownBy(() -> gestor.responderBloque(SUAREZ_ACTIVO, paso1.cuestionarioId(), 1,
                respuestasMaximas(paso1)))
                .isInstanceOf(ReglaNegocioException.class);

        // Paso 8: último bloque
        CuestionarioWizardDTO fin = gestor.responderBloque(SUAREZ_ACTIVO, paso1.cuestionarioId(), 2,
                respuestasMaximas(paso2));
        assertThat(fin.completado()).isTrue();
        assertThat(fin.estado()).isEqualTo(EstadoCuestionario.COMPLETO);
        assertThat(fin.preguntas()).isEmpty();

        Cuestionario c = cuestionarioDAO.findById(paso1.cuestionarioId()).orElseThrow();
        assertThat(c.getFechaFinalizacion()).isNotNull();
        // Obs. 3 del CU 026: factor = suma / 2, competencia = promedio de factores, total = promedio
        assertThat(c.getResultado().getPuntajeObtenido()).isEqualTo(esperado);
        assertThat(c.getResultado().getResultadosCompetencia()).hasSize(2);
        assertThat(c.getResultado().fueAprobado()).isTrue();
    }

    @Test
    @DisplayName("1.C: el reingreso se posiciona en el bloque pendiente con las mismas preguntas")
    void reingreso() {
        CuestionarioWizardDTO paso = gestor.iniciarOReanudarCuestionario(TORRES_EN_PROCESO);

        assertThat(paso.numeroBloque()).isEqualTo(2);
        assertThat(paso.preguntasRespondidas()).isEqualTo(2);
        assertThat(paso.preguntas()).extracting(PreguntaCuestionarioDTO::nroOrden).containsExactly(3, 4);

        Cuestionario c = cuestionarioDAO.findById(paso.cuestionarioId()).orElseThrow();
        assertThat(c.getCantidadAccesos()).isEqualTo(2);
    }

    @Test
    @DisplayName("1.A: en proceso con el tiempo máximo superado pasa a INCOMPLETO")
    void tiempoMaximoSuperado() {
        Cuestionario abierto = cuestionarioDAO.obtenerActivoOEnProceso(TORRES_EN_PROCESO).orElseThrow();
        abierto.setFechaInicio(LocalDateTime.now().minusHours(2));   // máximo: 60 minutos
        Long id = abierto.getId();

        assertThatThrownBy(() -> gestor.iniciarOReanudarCuestionario(TORRES_EN_PROCESO))
                .isInstanceOf(CuestionarioNoDisponibleException.class)
                .hasMessageContaining("tiempo máximo");

        Cuestionario c = cuestionarioDAO.findById(id).orElseThrow();
        assertThat(c.getEstado()).isEqualTo(EstadoCuestionario.INCOMPLETO);
        assertThat(c.getFechaCambioEstado()).isNotNull();
    }

    @Test
    @DisplayName("Precondición: superada la cantidad máxima de accesos pasa a INCOMPLETO")
    void maximoAccesosSuperado() {
        Cuestionario c = cuestionarioDAO.obtenerActivoOEnProceso(TORRES_EN_PROCESO).orElseThrow();
        c.setCantidadAccesos(5);

        assertThatThrownBy(() -> gestor.iniciarOReanudarCuestionario(TORRES_EN_PROCESO))
                .isInstanceOf(CuestionarioNoDisponibleException.class)
                .hasMessageContaining("accesos");
        assertThat(c.getEstado()).isEqualTo(EstadoCuestionario.INCOMPLETO);
    }

    @Test
    @DisplayName("1.B: activo más tiempo del permitido pasa a SIN_CONTESTAR")
    void tiempoActivoSuperado() {
        Cuestionario c = cuestionarioDAO.obtenerActivoOEnProceso(SUAREZ_ACTIVO).orElseThrow();
        c.setFechaCreacion(LocalDateTime.now().minusDays(10));   // máximo: 168 horas

        assertThatThrownBy(() -> gestor.iniciarOReanudarCuestionario(SUAREZ_ACTIVO))
                .isInstanceOf(CuestionarioNoDisponibleException.class)
                .extracting(e -> ((CuestionarioNoDisponibleException) e).getCodigo())
                .isEqualTo(CuestionarioNoDisponibleException.SIN_CONTESTAR);
        assertThat(c.getEstado()).isEqualTo(EstadoCuestionario.SIN_CONTESTAR);
    }

    @Test
    @DisplayName("Cierre automático: cierra solo los vencidos (EN_PROCESO -> INCOMPLETO, ACTIVO -> SIN_CONTESTAR)")
    void finalizarCuestionariosVencidos() {
        Cuestionario torres = cuestionarioDAO.obtenerActivoOEnProceso(TORRES_EN_PROCESO).orElseThrow();
        Cuestionario suarez = cuestionarioDAO.obtenerActivoOEnProceso(SUAREZ_ACTIVO).orElseThrow();
        torres.setFechaInicio(LocalDateTime.now().minusHours(2));
        suarez.setFechaCreacion(LocalDateTime.now().minusDays(10));

        int cerrados = gestor.finalizarCuestionariosVencidos();

        assertThat(cerrados).isEqualTo(2);
        assertThat(torres.getEstado()).isEqualTo(EstadoCuestionario.INCOMPLETO);
        assertThat(suarez.getEstado()).isEqualTo(EstadoCuestionario.SIN_CONTESTAR);
        assertThat(torres.getFechaCambioEstado()).isNotNull();
        assertThat(suarez.getFechaCambioEstado()).isNotNull();
    }

    @Test
    @DisplayName("Cierre automático: no toca los cuestionarios que siguen vigentes")
    void finalizarCuestionariosVencidosSinVencidos() {
        assertThat(gestor.finalizarCuestionariosVencidos()).isZero();

        assertThat(cuestionarioDAO.obtenerActivoOEnProceso(TORRES_EN_PROCESO).orElseThrow().getEstado())
                .isEqualTo(EstadoCuestionario.EN_PROCESO);
        assertThat(cuestionarioDAO.obtenerActivoOEnProceso(SUAREZ_ACTIVO).orElseThrow().getEstado())
                .isEqualTo(EstadoCuestionario.ACTIVO);
    }

    @Test
    @DisplayName("Seed: los cuestionarios vencidos ya figuran cerrados y no permiten ingresar")
    void vencidosDelSeedCerrados() {
        assertThat(cuestionarioDAO.obtenerActivoOEnProceso(MEDINA_INCOMPLETO)).isEmpty();
        assertThat(cuestionarioDAO.obtenerActivoOEnProceso(HERRERA_SIN_CONTESTAR)).isEmpty();
        assertThat(porClave("Medina17").getEstado()).isEqualTo(EstadoCuestionario.INCOMPLETO);
        assertThat(porClave("Herrer18").getEstado()).isEqualTo(EstadoCuestionario.SIN_CONTESTAR);

        assertThatThrownBy(() -> gestor.iniciarOReanudarCuestionario(MEDINA_INCOMPLETO))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("fueAprobado: una competencia del puesto sin resultado cuenta como no aprobada")
    void noApruebaSinTodasLasCompetencias() {
        Cuestionario sosa = porClave("Sosa0009");
        assertThat(sosa.getResultado().fueAprobado()).isTrue();

        // Se quita la competencia COMUNICACIÓN: la de TEC sigue superando el mínimo
        sosa.getResultado().getResultadosCompetencia()
                .removeIf(rc -> rc.getCaracteristicaPuesto().getCompetencia().getCodigo().startsWith("COM"));

        assertThat(sosa.getResultado().getResultadosCompetencia()).hasSize(1);
        assertThat(sosa.getResultado().fueAprobado()).isFalse();
    }

    @Test
    @DisplayName("Un candidato sin cuestionario abierto no puede iniciar")
    void sinCuestionarioAbierto() {
        assertThatThrownBy(() -> gestor.iniciarOReanudarCuestionario(9L))   // SOSA: COMPLETO
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("Un candidato no puede responder el cuestionario de otro")
    void noPuedeResponderAjeno() {
        CuestionarioWizardDTO paso = gestor.iniciarOReanudarCuestionario(SUAREZ_ACTIVO);

        assertThatThrownBy(() -> gestor.responderBloque(TORRES_EN_PROCESO, paso.cuestionarioId(), 1,
                responderPrimeraOpcion(paso)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("Datos del seed para el CU 028: resultados ya calculados")
    void resultadosDelSeed() {
        // Totales con las ponderaciones actuales del seed (TEC 7/3, COMUNICACIÓN 0/3/7)
        Cuestionario sosa = porClave("Sosa0009");
        assertThat(sosa.getEstado()).isEqualTo(EstadoCuestionario.COMPLETO);
        assertThat(sosa.getResultado().getPuntajeObtenido()).isEqualTo(7.0);
        assertThat(sosa.getResultado().fueAprobado()).isTrue();

        Cuestionario acosta = porClave("Acos0011");
        assertThat(acosta.getResultado().getPuntajeObtenido()).isEqualTo(5.25);
        assertThat(acosta.getResultado().fueAprobado()).isFalse();
    }

    /** Para cada pregunta del bloque elige la opción con mayor ponderación. */
    private List<RespuestaBloqueDTO> respuestasMaximas(CuestionarioWizardDTO paso) {
        Map<Long, OpcionRespuesta> mejores = new HashMap<>();
        asignadas(paso.cuestionarioId()).forEach(pa -> mejores.put(pa.getId(), mejorOpcion(pa)));
        return paso.preguntas().stream()
                .map(p -> new RespuestaBloqueDTO(p.preguntaAsignadaId(), mejores.get(p.preguntaAsignadaId()).getId()))
                .toList();
    }

    private List<PreguntaAsignada> asignadas(Long cuestionarioId) {
        return cuestionarioDAO.findById(cuestionarioId).orElseThrow().getBloques().stream()
                .flatMap(b -> b.getPreguntas().stream())
                .toList();
    }

    private static OpcionRespuesta mejorOpcion(PreguntaAsignada pa) {
        return pa.getPregunta().getOpciones().stream()
                .max(Comparator.comparing(OpcionRespuesta::getPonderacion))
                .orElseThrow();
    }

    /** Puntaje total si se elige la mejor opción en todas las preguntas asignadas (obs. 3 del CU 026). */
    private double puntajeConMejoresOpciones(Long cuestionarioId) {
        Map<Long, Map<Long, Double>> sumaPorFactorDeCompetencia = new HashMap<>();
        asignadas(cuestionarioId).forEach(pa -> sumaPorFactorDeCompetencia
                .computeIfAbsent(pa.getPregunta().getFactor().getCompetencia().getId(), k -> new HashMap<>())
                .merge(pa.getPregunta().getFactor().getId(), mejorOpcion(pa).getPonderacion(), Double::sum));
        return sumaPorFactorDeCompetencia.values().stream()
                .mapToDouble(factores -> factores.values().stream().mapToDouble(suma -> suma / 2).average().orElseThrow())
                .average()
                .orElseThrow();
    }
}
