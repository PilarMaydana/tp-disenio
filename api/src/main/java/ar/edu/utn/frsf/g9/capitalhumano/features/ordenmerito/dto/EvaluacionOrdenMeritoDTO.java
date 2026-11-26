package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto;

public record EvaluacionOrdenMeritoDTO(
        Long id,
        String fecha,
        String fechaISO,
        String proyecto,
        String estado
) { }
