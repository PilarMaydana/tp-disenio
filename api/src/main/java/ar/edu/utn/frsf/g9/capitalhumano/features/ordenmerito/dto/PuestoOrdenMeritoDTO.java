package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto;

public record PuestoOrdenMeritoDTO(
        Long id,
        String codigo,
        String nombrePuesto,
        String empresaId,
        String empresaNombre,
        String descripcion,
        long candidatos,
        long evaluacionesCompletas
) { }
