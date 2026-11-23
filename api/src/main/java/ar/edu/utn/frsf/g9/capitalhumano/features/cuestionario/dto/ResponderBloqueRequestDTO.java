package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import jakarta.validation.Valid;

import java.util.List;

/**
 * Respuestas del bloque enviado por el candidato (CU 026 - mensaje 47).
 *
 * @param respuestas respuestas a las preguntas del bloque
 */
public record ResponderBloqueRequestDTO(@Valid List<RespuestaBloqueDTO> respuestas) {
}
