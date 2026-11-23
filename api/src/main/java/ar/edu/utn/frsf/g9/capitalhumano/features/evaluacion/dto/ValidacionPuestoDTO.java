package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import java.util.List;

/**
 * Resultado de validar si un puesto puede ser evaluado (CU 025 - paso 9 / flujo 9.A).
 *
 * @param puestoCodigo             código del puesto validado
 * @param evaluable                {@code true} si todas sus competencias pueden evaluarse
 * @param competenciasNoEvaluables nombres de las competencias sin factores con al menos 2 preguntas
 */
public record ValidacionPuestoDTO(
        String puestoCodigo,
        boolean evaluable,
        List<String> competenciasNoEvaluables
) {
}
