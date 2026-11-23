package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Respuesta a una pregunta del bloque.
 *
 * @param preguntaAsignadaId pregunta respondida
 * @param opcionId           opción elegida
 */
public record RespuestaBloqueDTO(
        @NotNull Long preguntaAsignadaId,
        Long opcionId
) {
}
