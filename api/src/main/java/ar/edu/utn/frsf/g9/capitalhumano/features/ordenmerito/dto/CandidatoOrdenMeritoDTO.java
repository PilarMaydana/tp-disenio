package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto;

import java.time.LocalDateTime;
import java.util.List;

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
        List<IncumplimientoMinimoDTO> minimosNoAlcanzados,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFinalizacion,
        String datosAdicionales
) {
    public record IncumplimientoMinimoDTO(String competencia, Double puntajeObtenido, Double puntajeMinimo) { }
}
