package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import java.util.List;

/** Pregunta, competencia y respuesta guardada para reanudar el flujo CU028. */
public record PreguntaRespuestaCuestionarioDTO(
        Long id,
        String enunciado,
        String competencia,
        List<OpcionCuestionarioDTO> opciones,
        Long opcionSeleccionadaId) {
}
