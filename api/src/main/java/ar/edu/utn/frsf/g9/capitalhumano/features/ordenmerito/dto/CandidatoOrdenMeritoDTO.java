package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto;

import java.time.LocalDateTime;

public record CandidatoOrdenMeritoDTO(
        Long cuestionarioId,
        String tipoDocumento,
        String nroDocumento,
        String apellido,
        String nombre,
        String estadoCuestionario,
        Double puntajeObtenido,
        String estadoMerito,
        Boolean cumpleMinimos,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFinalizacion,
        String datosAdicionales
) { }
