package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

import java.util.List;

/** Resumen del cuestionario para el flujo CU028 de respuesta por competencia. */
public record CuestionarioRespuestaResumenDTO(
        Long nroCuestionario,
        String titulo,
        String puestoNombre,
        String empresaNombre,
        String estado,
        String fechaInicio,
        String fechaFin,
        String ultimaActualizacion,
        int totalPreguntas,
        int preguntasRespondidas,
        List<PuntajeCompetenciaDTO> puntajes) {
}
