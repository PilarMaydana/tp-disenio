package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import java.util.List;

public record GuardarCuestionarioDTO(String estado, int respondidas, int totalPreguntas,
                                    List<PuntajeCompetenciaDTO> puntajes) { }
