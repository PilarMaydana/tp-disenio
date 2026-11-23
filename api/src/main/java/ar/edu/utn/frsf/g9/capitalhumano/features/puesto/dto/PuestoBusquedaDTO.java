package ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dto;

/**
 * DTO resumido de un puesto para listas de selección (CU 025 - paso 8).
 *
 * @param id      identificador interno
 * @param codigo  código del puesto
 * @param nombre  nombre del puesto o función
 * @param empresa empresa
 */
public record PuestoBusquedaDTO(
        Long id,
        String codigo,
        String nombre,
        String empresa
) {
}
