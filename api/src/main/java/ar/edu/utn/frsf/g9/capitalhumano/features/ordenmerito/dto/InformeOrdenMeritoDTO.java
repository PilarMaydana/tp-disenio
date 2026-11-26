package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Datos completos que comparten los formatos de descarga del informe. */
public record InformeOrdenMeritoDTO(
        String codigoPuesto,
        String nombrePuesto,
        String empresa,
        String descripcionPuesto,
        LocalDateTime fechaGeneracion,
        List<EvaluacionOrdenMeritoDTO> evaluaciones,
        List<CandidatoOrdenMeritoDTO> candidatos
) { }
