package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import jakarta.validation.constraints.NotNull;

public record RespuestaSeleccionadaDTO(
        @NotNull Long preguntaId,
        @NotNull Long opcionId
) { }
