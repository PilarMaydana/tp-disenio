package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import java.util.List;

/**
 * Solicitud para validar candidatos y/o preparar la evaluación (CU 025 - pasos 6 y 10).
 *
 * @param nroCandidatos números de los candidatos seleccionados en la grilla "Candidatos a evaluar"
 * @param puestoCodigo  código del puesto o función (puede omitirse al validar sólo candidatos)
 */
public record EvaluarCandidatosRequestDTO(
        List<Long> nroCandidatos,
        String puestoCodigo
) {
}
