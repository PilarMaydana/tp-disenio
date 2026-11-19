package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import jakarta.validation.Valid;

import java.util.List;

/**
 * Solicitud para confirmar la evaluación al presionar "Finalizar" (CU 025 - paso 12).
 *
 * <p>Incluye las claves que el sistema generó y mostró en el paso 11, para que lo que se
 * guarda sea exactamente lo que vio el consultor.</p>
 *
 * @param puestoCodigo código del puesto o función
 * @param candidatos   candidatos a evaluar con su clave de ingreso
 */
public record ConfirmarEvaluacionRequestDTO(
        String puestoCodigo,
        @Valid List<CandidatoClaveDTO> candidatos
) {
}
