package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;

import java.util.List;

/**
 * Paso del wizard del cuestionario (CU 026 - mensajes 28, 44, 68 y 71).
 *
 * @param cuestionarioId       identificador del cuestionario
 * @param puestoNombre         puesto o función evaluada
 * @param empresa              empresa del puesto
 * @param estado               estado del cuestionario
 * @param numeroBloque         bloque a responder (null si se completó)
 * @param totalBloques         cantidad de bloques
 * @param totalPreguntas       cantidad de preguntas del cuestionario
 * @param preguntasRespondidas preguntas ya respondidas
 * @param segundosRestantes    tiempo restante para completarlo
 * @param completado           {@code true} si no quedan bloques (paso 8)
 * @param preguntas            preguntas del bloque a responder (vacío si se completó)
 */
public record CuestionarioWizardDTO(
        Long cuestionarioId,
        String puestoNombre,
        String empresa,
        EstadoCuestionario estado,
        Integer numeroBloque,
        int totalBloques,
        int totalPreguntas,
        int preguntasRespondidas,
        Long segundosRestantes,
        boolean completado,
        List<PreguntaCuestionarioDTO> preguntas
) {
}
