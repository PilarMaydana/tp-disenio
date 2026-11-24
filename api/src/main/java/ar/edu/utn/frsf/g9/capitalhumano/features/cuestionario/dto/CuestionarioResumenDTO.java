package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;

import java.time.LocalDateTime;

/**
 * Datos del cuestionario abierto del candidato, para la pantalla de ingreso (CU 026 - mensaje 1).
 *
 * @param cuestionarioId        identificador del cuestionario
 * @param estado                estado actual (ACTIVO o EN_PROCESO)
 * @param puestoNombre          puesto o función evaluada
 * @param empresa               empresa del puesto
 * @param fechaCreacion         fecha en que se generó (CU 025)
 * @param fechaInicio           primer ingreso (null si todavía no comenzó)
 * @param fechaUltimoIngreso    último ingreso
 * @param cantidadAccesos       ingresos realizados
 * @param cantidadMaximaAccesos ingresos permitidos
 * @param tiempoMaximoMinutos   tiempo máximo para completarlo
 * @param totalPreguntas        preguntas asignadas (0 si todavía no comenzó)
 * @param preguntasRespondidas  preguntas ya respondidas
 * @param segundosRestantes     tiempo restante (null si todavía no comenzó)
 */
public record CuestionarioResumenDTO(
        Long cuestionarioId,
        EstadoCuestionario estado,
        String puestoNombre,
        String empresa,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaUltimoIngreso,
        Integer cantidadAccesos,
        Integer cantidadMaximaAccesos,
        Integer tiempoMaximoMinutos,
        int totalPreguntas,
        int preguntasRespondidas,
        Long segundosRestantes
) {
}
