package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

/**
 * Opción de respuesta que se muestra al candidato (sin su ponderación).
 *
 * @param id          identificador de la opción
 * @param descripcion texto de la opción
 */
public record OpcionCuestionarioDTO(Long id, String descripcion) {
}
