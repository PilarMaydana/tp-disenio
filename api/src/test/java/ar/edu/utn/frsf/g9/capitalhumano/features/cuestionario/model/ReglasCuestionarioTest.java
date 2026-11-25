package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valores límite de las reglas del CU 026 (casos TC-026-04 y TC-026-05 de la auditoría de QA).
 */
class ReglasCuestionarioTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 25, 10, 0, 0);

    @Nested
    @DisplayName("1.A - Tiempo máximo para completar (60 minutos)")
    class TiempoMaximo {

        private Cuestionario iniciadoHace(long segundos) {
            Cuestionario c = new Cuestionario();
            c.setTiempoMaximoMinutos(60);
            c.setFechaInicio(AHORA.minusSeconds(segundos));
            return c;
        }

        @Test
        @DisplayName("Exactamente 3600 s desde el inicio: todavía puede responder")
        void justoEnElLimite() {
            assertThat(iniciadoHace(3600).superaTiempoMaximo(AHORA)).isFalse();
        }

        @Test
        @DisplayName("3601 s desde el inicio: superó el tiempo máximo")
        void unSegundoDespues() {
            assertThat(iniciadoHace(3601).superaTiempoMaximo(AHORA)).isTrue();
        }

        @Test
        @DisplayName("Sin fecha de inicio (nunca ingresó) no se considera vencido por llenado")
        void sinInicio() {
            Cuestionario c = new Cuestionario();
            c.setTiempoMaximoMinutos(60);
            assertThat(c.superaTiempoMaximo(AHORA)).isFalse();
        }
    }

    @Nested
    @DisplayName("1.B - Tiempo máximo en estado ACTIVO (168 horas)")
    class TiempoMaximoActivo {

        private Cuestionario creadoHace(long segundos) {
            Cuestionario c = new Cuestionario();
            c.setTiempoMaximoActivoHoras(168);
            c.setFechaCreacion(AHORA.minusSeconds(segundos));
            return c;
        }

        @Test
        @DisplayName("Exactamente 168 h desde la creación: todavía puede comenzar")
        void justoEnElLimite() {
            assertThat(creadoHace(168 * 3600L).superaTiempoMaximoActivo(AHORA)).isFalse();
        }

        @Test
        @DisplayName("168 h y 1 s desde la creación: superó el tiempo en estado activo")
        void unSegundoDespues() {
            assertThat(creadoHace(168 * 3600L + 1).superaTiempoMaximoActivo(AHORA)).isTrue();
        }
    }

    @Nested
    @DisplayName("Aprobación: total de la competencia contra la ponderación requerida")
    class Aprobacion {

        /** Resultado de una competencia con requerida 5.0 y un factor con las dos respuestas indicadas. */
        private ResultadoCompetencia competenciaCon(double respuesta1, double respuesta2) {
            CaracteristicaPuesto caracteristica = new CaracteristicaPuesto(1L, null, null, 5.0);
            ResultadoFactor factor = new ResultadoFactor();
            factor.calcularTotal(List.of(respuesta(respuesta1), respuesta(respuesta2)));
            ResultadoCompetencia competencia = new ResultadoCompetencia(caracteristica);
            competencia.agregarResultadoFactor(factor);
            competencia.calcularTotal();
            return competencia;
        }

        private RespuestaCandidato respuesta(double ponderacion) {
            return new RespuestaCandidato(null, new OpcionRespuesta(null, 1, "OPCION", null, ponderacion), AHORA);
        }

        @Test
        @DisplayName("Total igual a la requerida (5.0 = 5.0): aprueba (condición >=)")
        void igualALaRequerida() {
            ResultadoCompetencia rc = competenciaCon(10.0, 0.0);   // (10 + 0) / 2 = 5.0
            assertThat(rc.getTotalCompetencia()).isEqualTo(5.0);
            assertThat(rc.superaMinimoRequerido()).isTrue();

            Resultado resultado = new Resultado(AHORA);
            resultado.agregarResultadoCompetencia(rc);
            assertThat(resultado.fueAprobado()).isTrue();
        }

        @Test
        @DisplayName("Total apenas por debajo de la requerida (4.5 < 5.0): no aprueba")
        void debajoDeLaRequerida() {
            ResultadoCompetencia rc = competenciaCon(7.0, 2.0);    // (7 + 2) / 2 = 4.5
            assertThat(rc.superaMinimoRequerido()).isFalse();

            Resultado resultado = new Resultado(AHORA);
            resultado.agregarResultadoCompetencia(rc);
            assertThat(resultado.fueAprobado()).isFalse();
        }
    }
}
