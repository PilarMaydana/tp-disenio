package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.CandidatoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Aplica las reglas de mérito y el orden de prioridad de los candidatos. */
@Service
public class ClasificadorMerito {

    public List<CandidatoOrdenMeritoDTO> clasificar(List<Cuestionario> cuestionarios, Puesto puesto,
                                                      Map<Long, Map<Long, Double>> puntajesPorCuestionario) {
        return cuestionarios.stream()
                .map(cuestionario -> clasificar(cuestionario, puesto,
                        puntajesPorCuestionario.getOrDefault(cuestionario.getId(), Map.of())))
                .sorted(Comparator.comparingInt((CandidatoOrdenMeritoDTO resultado) -> prioridad(resultado.estadoMerito()))
                        .thenComparing(CandidatoOrdenMeritoDTO::puntajeObtenido,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private CandidatoOrdenMeritoDTO clasificar(Cuestionario cuestionario, Puesto puesto,
                                                Map<Long, Double> puntajes) {
        boolean completo = cuestionario.getEstado() != null
                && cuestionario.getEstado().name().equals("COMPLETO");
        boolean tieneTodosLosPuntajes = !puesto.getCaracteristicas().isEmpty()
                && puesto.getCaracteristicas().stream()
                .allMatch(caracteristica -> puntajes.containsKey(caracteristica.getCompetencia().getId()));

        Double total = tieneTodosLosPuntajes
                ? puesto.getCaracteristicas().stream()
                .mapToDouble(caracteristica -> puntajes.get(caracteristica.getCompetencia().getId()))
                .sum()
                : null;

        Boolean cumpleMinimos = null;
        String estadoMerito;
        if (!completo) {
            estadoMerito = "CUESTIONARIO_INCOMPLETO";
        } else if (!tieneTodosLosPuntajes) {
            estadoMerito = "PENDIENTE_PUNTAJE";
        } else {
            cumpleMinimos = puesto.getCaracteristicas().stream().allMatch(caracteristica ->
                    puntajes.get(caracteristica.getCompetencia().getId()) >= caracteristica.getPonderacionRequerida());
            estadoMerito = cumpleMinimos ? "APTO" : "NO_ALCANZA_MINIMOS";
        }

        var candidato = cuestionario.getCandidato();
        return new CandidatoOrdenMeritoDTO(cuestionario.getId(), candidato.getTipoDocumento().name(),
                candidato.getNroDocumento(), candidato.getApellido(), candidato.getNombre(),
                cuestionario.getEstado() == null ? "SIN_ESTADO" : cuestionario.getEstado().name(), total,
                estadoMerito, cumpleMinimos, cuestionario.getFechaInicio(), cuestionario.getFechaFinalizacion(),
                candidato.getEscolaridad());
    }

    private int prioridad(String estadoMerito) {
        return switch (estadoMerito) {
            case "APTO" -> 0;
            case "NO_ALCANZA_MINIMOS" -> 1;
            case "PENDIENTE_PUNTAJE" -> 2;
            default -> 3;
        };
    }
}
