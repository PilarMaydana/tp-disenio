package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import java.util.List;

/**
 * Pregunta del bloque que se muestra en el wizard.
 *
 * @param preguntaAsignadaId identificador de la pregunta dentro del cuestionario
 * @param nroOrden           número de la pregunta en el cuestionario
 * @param enunciado          texto de la pregunta
 * @param opciones           opciones de respuesta
 */
public record PreguntaCuestionarioDTO(
        Long preguntaAsignadaId,
        Integer nroOrden,
        String enunciado,
        List<OpcionCuestionarioDTO> opciones
) {
}
