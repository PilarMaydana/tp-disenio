package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dao.CuestionarioOrdenMeritoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dao.ResultadoCompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.CandidatoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.model.ResultadoCompetencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrdenMeritoControllerTest {

    private PuestoDAO puestoDAO;
    private CuestionarioOrdenMeritoDAO cuestionarioDAO;
    private ResultadoCompetenciaDAO resultadoDAO;
    private OrdenMeritoController controller;
    private Competencia competencia;

    @BeforeEach
    void setUp() {
        puestoDAO = mock(PuestoDAO.class);
        cuestionarioDAO = mock(CuestionarioOrdenMeritoDAO.class);
        resultadoDAO = mock(ResultadoCompetenciaDAO.class);
        controller = new OrdenMeritoController(puestoDAO, cuestionarioDAO, resultadoDAO);
        Puesto puesto = new Puesto(10L, "DEV", "Desarrollador", "Descripción", "Empresa");
        competencia = new Competencia(7L, "TEC", "Técnica", "Competencia técnica");
        puesto.addCaracteristica(new CaracteristicaPuesto(1L, puesto, competencia, 6.0));
        when(puestoDAO.findById(10L)).thenReturn(Optional.of(puesto));
    }

    @Test
    void reporteClasificaYOrdenaAptosPorPuntaje() {
        Cuestionario primero = cuestionario(1L, EstadoCuestionario.COMPLETO, "Perez");
        Cuestionario segundo = cuestionario(2L, EstadoCuestionario.COMPLETO, "Gomez");
        when(cuestionarioDAO.findByPuestoIdAndEliminadoFalseOrderByIdAsc(10L)).thenReturn(List.of(primero, segundo));
        when(resultadoDAO.findByCuestionarioIdIn(List.of(1L, 2L))).thenReturn(List.of(
                resultado(primero, 8.0), resultado(segundo, 9.0)));

        List<CandidatoOrdenMeritoDTO> reporte = controller.obtenerReporte(10L, List.of(1L, 2L));

        assertEquals(List.of(2L, 1L), reporte.stream().map(CandidatoOrdenMeritoDTO::cuestionarioId).toList());
        assertEquals("APTO", reporte.getFirst().estadoMerito());
        assertEquals(9.0, reporte.getFirst().puntajeObtenido());
    }

    @Test
    void reporteDistinguePuntajePendienteYCuestionarioIncompleto() {
        Cuestionario sinPuntaje = cuestionario(1L, EstadoCuestionario.COMPLETO, "Perez");
        Cuestionario incompleto = cuestionario(2L, EstadoCuestionario.INCOMPLETO, "Gomez");
        when(cuestionarioDAO.findByPuestoIdAndEliminadoFalseOrderByIdAsc(10L)).thenReturn(List.of(sinPuntaje, incompleto));
        when(resultadoDAO.findByCuestionarioIdIn(List.of(1L, 2L))).thenReturn(List.of());

        List<CandidatoOrdenMeritoDTO> reporte = controller.obtenerReporte(10L, List.of(1L, 2L));

        assertEquals("PENDIENTE_PUNTAJE", reporte.get(0).estadoMerito());
        assertNull(reporte.get(0).puntajeObtenido());
        assertEquals("CUESTIONARIO_INCOMPLETO", reporte.get(1).estadoMerito());
    }

    @Test
    void reporteMarcaComoNoAptoCuandoUnaCompetenciaNoLlegaAlMinimo() {
        Cuestionario candidato = cuestionario(1L, EstadoCuestionario.COMPLETO, "Perez");
        when(cuestionarioDAO.findByPuestoIdAndEliminadoFalseOrderByIdAsc(10L)).thenReturn(List.of(candidato));
        when(resultadoDAO.findByCuestionarioIdIn(List.of(1L))).thenReturn(List.of(resultado(candidato, 5.99)));

        CandidatoOrdenMeritoDTO fila = controller.obtenerReporte(10L, List.of(1L)).getFirst();

        assertEquals("NO_ALCANZA_MINIMOS", fila.estadoMerito());
        assertEquals(false, fila.cumpleMinimos());
    }

    @Test
    void reporteRechazaCuestionarioQueNoPerteneceAlPuesto() {
        when(cuestionarioDAO.findByPuestoIdAndEliminadoFalseOrderByIdAsc(10L)).thenReturn(List.of());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.obtenerReporte(10L, List.of(99L)));

        assertEquals(400, error.getStatusCode().value());
    }

    private Cuestionario cuestionario(Long id, EstadoCuestionario estado, String apellido) {
        Candidato candidato = new Candidato(id, id, TipoDocumento.DNI, "12345678", apellido, "Ana", "a@b.com");
        return new Cuestionario(id, candidato, estado, "ABCD1234");
    }

    private ResultadoCompetencia resultado(Cuestionario cuestionario, double puntaje) {
        return new ResultadoCompetencia(cuestionario, competencia, puntaje);
    }
}
