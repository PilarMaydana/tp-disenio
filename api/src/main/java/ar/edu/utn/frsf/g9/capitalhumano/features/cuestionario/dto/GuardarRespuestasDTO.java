package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record GuardarRespuestasDTO(@NotEmpty List<@Valid RespuestaSeleccionadaDTO> respuestas) { }
