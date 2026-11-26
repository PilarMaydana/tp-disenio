package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.CandidatoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClasificadorMeritoTest {
    private static final Long COMPETENCIA_ID = 7L;

    private ClasificadorMerito clasificador;
    private Puesto puesto;

    @BeforeEach
    void setUp() {
        clasificador = new ClasificadorMerito();
        puesto = new Puesto(10L, "DEV", "Desarrollador", "Descripción", "Empresa");
        Competencia competencia = new Competencia(COMPETENCIA_ID, "TEC", "Técnica", "Competencia técnica");
        puesto.addCaracteristica(new CaracteristicaPuesto(1L, puesto, competencia, 6.0));
    }

    @Test
    void clasificaComoAptoCuandoCompletoAlcanzaElMinimo() {
        CandidatoOrdenMeritoDTO resultado = clasificar(
                cuestionario(1L, EstadoCuestionario.COMPLETO), 8.0);

        assertEquals("APTO", resultado.estadoMerito());
        assertEquals(8.0, resultado.puntajeObtenido());
        assertTrue(resultado.cumpleMinimos());
    }

    @Test
    void clasificaComoNoAptoCuandoNoAlcanzaElMinimo() {
        CandidatoOrdenMeritoDTO resultado = clasificar(
                cuestionario(1L, EstadoCuestionario.COMPLETO), 5.99);

        assertEquals("NO_ALCANZA_MINIMOS", resultado.estadoMerito());
        assertFalse(resultado.cumpleMinimos());
    }

    @Test
    void clasificaComoPendienteCuandoFaltaUnPuntaje() {
        CandidatoOrdenMeritoDTO resultado = clasificar(cuestionario(1L, EstadoCuestionario.COMPLETO), null);

        assertEquals("PENDIENTE_PUNTAJE", resultado.estadoMerito());
        assertNull(resultado.puntajeObtenido());
        assertNull(resultado.cumpleMinimos());
    }

    @Test
    void cuestionarioIncompletoTienePrioridadSobreSusPuntajes() {
        CandidatoOrdenMeritoDTO resultado = clasificar(
                cuestionario(1L, EstadoCuestionario.INCOMPLETO), 9.0);

        assertEquals("CUESTIONARIO_INCOMPLETO", resultado.estadoMerito());
        assertNull(resultado.cumpleMinimos());
    }

    @Test
    void ordenaPorCondicionYLuegoPorPuntajeDescendente() {
        Cuestionario aptoPuntajeMenor = cuestionario(1L, EstadoCuestionario.COMPLETO);
        Cuestionario aptoPuntajeMayor = cuestionario(2L, EstadoCuestionario.COMPLETO);
        Cuestionario noApto = cuestionario(3L, EstadoCuestionario.COMPLETO);
        Cuestionario pendiente = cuestionario(4L, EstadoCuestionario.COMPLETO);
        Cuestionario incompleto = cuestionario(5L, EstadoCuestionario.INCOMPLETO);

        List<CandidatoOrdenMeritoDTO> resultados = clasificador.clasificar(
                List.of(aptoPuntajeMenor, aptoPuntajeMayor, noApto, pendiente, incompleto), puesto,
                Map.of(1L, puntajes(7.0), 2L, puntajes(9.0), 3L, puntajes(5.0), 5L, puntajes(10.0)));

        assertEquals(List.of(2L, 1L, 3L, 4L, 5L),
                resultados.stream().map(CandidatoOrdenMeritoDTO::cuestionarioId).toList());
    }

    private CandidatoOrdenMeritoDTO clasificar(Cuestionario cuestionario, Double puntaje) {
        Map<Long, Map<Long, Double>> puntajes = puntaje == null
                ? Map.of()
                : Map.of(cuestionario.getId(), puntajes(puntaje));
        return clasificador.clasificar(List.of(cuestionario), puesto, puntajes).getFirst();
    }

    private Map<Long, Double> puntajes(double valor) {
        return Map.of(COMPETENCIA_ID, valor);
    }

    private Cuestionario cuestionario(Long id, EstadoCuestionario estado) {
        Candidato candidato = new Candidato(id, id, TipoDocumento.DNI, "12345678", "Apellido", "Nombre", "a@b.com");
        return new Cuestionario(id, candidato, estado, "ABCD1234");
    }
}
