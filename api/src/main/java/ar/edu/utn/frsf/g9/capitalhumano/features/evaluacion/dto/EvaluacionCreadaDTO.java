package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resultado de guardar la evaluación (CU 025 - paso 13).
 *
 * @param evaluacionId   identificador de la evaluación creada
 * @param puestoCodigo   código del puesto
 * @param puestoNombre   nombre del puesto
 * @param empresa        empresa del puesto
 * @param fechaCreacion  fecha y hora de creación
 * @param candidatos     candidatos evaluados con sus claves
 */
public record EvaluacionCreadaDTO(
        Long evaluacionId,
        String puestoCodigo,
        String puestoNombre,
        String empresa,
        LocalDateTime fechaCreacion,
        List<CandidatoClaveDTO> candidatos
) {
}
