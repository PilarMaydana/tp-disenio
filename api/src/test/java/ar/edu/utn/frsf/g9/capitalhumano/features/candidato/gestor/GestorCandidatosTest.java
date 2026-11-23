package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dao.CandidatoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GestorCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao.EvaluacionDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CandidatoClaveDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ConfirmarEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluacionCreadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluarCandidatosRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ValidacionPuestoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao.FactorDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.security.ContextoSeguridad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para {@link GestorCandidatos} (CU 025 - Evaluar a Candidatos).
 */
@ExtendWith(MockitoExtension.class)
class GestorCandidatosTest {

    @Mock
    private CandidatoDAO candidatoDAO;
    @Mock
    private CuestionarioDAO cuestionarioDAO;
    @Mock
    private PuestoDAO puestoDAO;
    @Mock
    private FactorDAO factorDAO;
    @Mock
    private EvaluacionDAO evaluacionDAO;
    @Mock
    private GestorCuestionario gestorCuestionario;
    @Mock
    private ContextoSeguridad contextoSeguridad;

    @InjectMocks
    private GestorCandidatos gestor;

    // ----------------------------------------------------------- fixtures

    private static Candidato candidato(long nro, String apellido, String nombre) {
        return new Candidato(nro, nro, TipoDocumento.DNI, "4000000" + nro, apellido, nombre, null);
    }

    private static Factor factorConPreguntas(long id, Competencia competencia, int cantidadPreguntas) {
        Factor f = new Factor(id, "FAC-" + id, "FACTOR " + id, "DESC", (int) id, competencia);
        List<Pregunta> preguntas = new ArrayList<>();
        for (int i = 0; i < cantidadPreguntas; i++) {
            Pregunta p = new Pregunta();
            p.setEliminado(false);
            preguntas.add(p);
        }
        f.setPreguntas(preguntas);
        return f;
    }

    private Puesto puestoCon(Competencia... competencias) {
        Puesto puesto = new Puesto(10L, "BACK-01", "DESARROLLADOR BACKEND", "DESC", "TECH");
        for (Competencia c : competencias) {
            puesto.addCaracteristica(new CaracteristicaPuesto(null, null, c, 7.0));
        }
        return puesto;
    }

    private void candidatoDisponible(Candidato c) {
        when(candidatoDAO.findByNroCandidatoAndEliminadoFalse(c.getNroCandidato())).thenReturn(Optional.of(c));
        when(cuestionarioDAO.tieneCuestionarioActivoOProceso(c.getNroCandidato())).thenReturn(false);
    }

    // ----------------------------------------------------------- claves

    @RepeatedTest(20)
    @DisplayName("generarClaveAcceso: 8 caracteres alfanuméricos con al menos una letra y un dígito")
    void generarClaveAcceso_formatoValido() {
        String clave = gestor.generarClaveAcceso();
        assertThat(clave).hasSize(8).matches("^[A-Za-z0-9]{8}$").matches(".*[A-Za-z].*").matches(".*[0-9].*");
    }

    // ----------------------------------------------------------- 4.A / 6.A

    @Test
    @DisplayName("4.A: sin candidatos seleccionados se informa que debe seleccionarse uno o más")
    void verificarCandidatos_sinSeleccion() {
        assertThatThrownBy(() -> gestor.verificarCandidatosDisponibles(List.of()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Debe seleccionar uno o más candidatos");
    }

    @Test
    @DisplayName("6.A: se informa quiénes tienen cuestionarios activos o en proceso")
    void verificarCandidatos_conCuestionarioActivo() {
        Candidato juan = candidato(1, "PÉREZ", "JUAN");
        Candidato ana = candidato(2, "GÓMEZ", "ANA");
        when(candidatoDAO.findByNroCandidatoAndEliminadoFalse(1L)).thenReturn(Optional.of(juan));
        when(candidatoDAO.findByNroCandidatoAndEliminadoFalse(2L)).thenReturn(Optional.of(ana));
        when(cuestionarioDAO.tieneCuestionarioActivoOProceso(1L)).thenReturn(true);
        when(cuestionarioDAO.tieneCuestionarioActivoOProceso(2L)).thenReturn(false);

        assertThatThrownBy(() -> gestor.verificarCandidatosDisponibles(List.of(1L, 2L)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("PÉREZ, JUAN (Nro. 1)")
                .hasMessageNotContaining("GÓMEZ");
    }

    @Test
    @DisplayName("Candidato inexistente lanza RecursoNoEncontradoException")
    void verificarCandidatos_inexistente() {
        when(candidatoDAO.findByNroCandidatoAndEliminadoFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gestor.verificarCandidatosDisponibles(List.of(99L)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("Candidatos disponibles: retorna la lista sin repetidos y en el orden recibido")
    void verificarCandidatos_ok() {
        Candidato juan = candidato(1, "PÉREZ", "JUAN");
        Candidato ana = candidato(2, "GÓMEZ", "ANA");
        candidatoDisponible(juan);
        candidatoDisponible(ana);

        var resultado = gestor.verificarCandidatosDisponibles(List.of(2L, 1L, 2L));

        assertThat(resultado).extracting(c -> c.nroCandidato()).containsExactly(2L, 1L);
    }

    // ----------------------------------------------------------- 9.A

    @Test
    @DisplayName("9.A: competencia sin factores con al menos 2 preguntas no es evaluable")
    void validarCompetencias_noEvaluable() {
        Competencia tecnica = new Competencia(5L, "TEC-01", "COMPETENCIAS TÉCNICAS", "D");
        Competencia equipo = new Competencia(3L, "TRAB-01", "TRABAJO EN EQUIPO", "D");
        Puesto puesto = puestoCon(tecnica, equipo);
        when(puestoDAO.findByCodigoAndEliminadoFalse("BACK-01")).thenReturn(Optional.of(puesto));
        when(factorDAO.findByCompetenciaIdOrderByNroOrdenAsc(5L)).thenReturn(List.of(factorConPreguntas(1, tecnica, 2)));
        when(factorDAO.findByCompetenciaIdOrderByNroOrdenAsc(3L)).thenReturn(List.of(factorConPreguntas(2, equipo, 1)));

        ValidacionPuestoDTO resultado = gestor.validarCompetenciasEvaluables("back-01");

        assertThat(resultado.evaluable()).isFalse();
        assertThat(resultado.competenciasNoEvaluables()).containsExactly("TRABAJO EN EQUIPO");
    }

    @Test
    @DisplayName("Las preguntas eliminadas no cuentan para que un factor sea evaluable")
    void validarCompetencias_preguntasEliminadasNoCuentan() {
        Competencia tecnica = new Competencia(5L, "TEC-01", "COMPETENCIAS TÉCNICAS", "D");
        Factor factor = factorConPreguntas(1, tecnica, 2);
        factor.getPreguntas().get(1).setEliminado(true);
        when(puestoDAO.findByCodigoAndEliminadoFalse("BACK-01")).thenReturn(Optional.of(puestoCon(tecnica)));
        when(factorDAO.findByCompetenciaIdOrderByNroOrdenAsc(5L)).thenReturn(List.of(factor));

        assertThat(gestor.validarCompetenciasEvaluables("BACK-01").evaluable()).isFalse();
    }

    @Test
    @DisplayName("Puesto con todas las competencias evaluables")
    void validarCompetencias_evaluable() {
        Competencia tecnica = new Competencia(5L, "TEC-01", "COMPETENCIAS TÉCNICAS", "D");
        when(puestoDAO.findByCodigoAndEliminadoFalse("BACK-01")).thenReturn(Optional.of(puestoCon(tecnica)));
        when(factorDAO.findByCompetenciaIdOrderByNroOrdenAsc(5L))
                .thenReturn(List.of(factorConPreguntas(1, tecnica, 0), factorConPreguntas(2, tecnica, 3)));

        ValidacionPuestoDTO resultado = gestor.validarCompetenciasEvaluables("BACK-01");

        assertThat(resultado.evaluable()).isTrue();
        assertThat(resultado.competenciasNoEvaluables()).isEmpty();
    }

    // ----------------------------------------------------------- 10.A

    @Test
    @DisplayName("10.A: sin función o puesto seleccionado se informa que debe seleccionarse una función")
    void preparar_sinPuesto() {
        Candidato juan = candidato(1, "PÉREZ", "JUAN");
        candidatoDisponible(juan);

        assertThatThrownBy(() -> gestor.prepararEvaluacion(new EvaluarCandidatosRequestDTO(List.of(1L), " ")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Debe seleccionar una función o puesto");
    }

    // ----------------------------------------------------------- paso 11

    @Test
    @DisplayName("Preparar: genera una clave distinta por candidato sin guardar nada")
    void preparar_generaClaves() {
        Competencia tecnica = new Competencia(5L, "TEC-01", "COMPETENCIAS TÉCNICAS", "D");
        Candidato juan = candidato(1, "PÉREZ", "JUAN");
        Candidato ana = candidato(2, "GÓMEZ", "ANA");
        candidatoDisponible(juan);
        candidatoDisponible(ana);
        when(puestoDAO.findByCodigoAndEliminadoFalse("BACK-01")).thenReturn(Optional.of(puestoCon(tecnica)));
        when(factorDAO.findByCompetenciaIdOrderByNroOrdenAsc(5L)).thenReturn(List.of(factorConPreguntas(1, tecnica, 2)));

        List<CandidatoClaveDTO> resultado = gestor.prepararEvaluacion(
                new EvaluarCandidatosRequestDTO(List.of(1L, 2L), "BACK-01"));

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(CandidatoClaveDTO::apellido).containsExactly("PÉREZ", "GÓMEZ");
        assertThat(resultado.get(0).claveAcceso()).hasSize(8).isNotEqualTo(resultado.get(1).claveAcceso());
        verify(gestorCuestionario, never()).generarCuestionariosParaEvaluacion(anyMap(), any(), any());
    }

    @Test
    @DisplayName("Preparar con puesto no evaluable lanza ReglaNegocioException (9.A)")
    void preparar_puestoNoEvaluable() {
        Competencia equipo = new Competencia(3L, "TRAB-01", "TRABAJO EN EQUIPO", "D");
        Candidato juan = candidato(1, "PÉREZ", "JUAN");
        candidatoDisponible(juan);
        when(puestoDAO.findByCodigoAndEliminadoFalse("BACK-01")).thenReturn(Optional.of(puestoCon(equipo)));
        when(factorDAO.findByCompetenciaIdOrderByNroOrdenAsc(3L)).thenReturn(List.of());

        assertThatThrownBy(() -> gestor.prepararEvaluacion(new EvaluarCandidatosRequestDTO(List.of(1L), "BACK-01")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("TRABAJO EN EQUIPO");
    }

    // ----------------------------------------------------------- paso 12-13

    @Test
    @DisplayName("Finalizar: usa las claves mostradas y delega en GestorCuestionario la creación de la evaluación")
    @SuppressWarnings("unchecked")
    void evaluarCandidatos_ok() {
        Competencia tecnica = new Competencia(5L, "TEC-01", "COMPETENCIAS TÉCNICAS", "D");
        Candidato juan = candidato(1, "PÉREZ", "JUAN");
        Candidato ana = candidato(2, "GÓMEZ", "ANA");
        candidatoDisponible(juan);
        candidatoDisponible(ana);
        Puesto puesto = puestoCon(tecnica);
        when(puestoDAO.findByCodigoAndEliminadoFalse("BACK-01")).thenReturn(Optional.of(puesto));
        when(factorDAO.findByCompetenciaIdOrderByNroOrdenAsc(5L)).thenReturn(List.of(factorConPreguntas(1, tecnica, 2)));
        when(contextoSeguridad.getUsuarioActualId()).thenReturn(7L);

        Evaluacion guardada = new Evaluacion(puesto, LocalDateTime.of(2026, 11, 19, 17, 30), 7L);
        guardada.setId(55L);
        when(gestorCuestionario.generarCuestionariosParaEvaluacion(anyMap(), eq(puesto), eq(7L))).thenReturn(guardada);

        ConfirmarEvaluacionRequestDTO request = new ConfirmarEvaluacionRequestDTO("BACK-01", List.of(
                new CandidatoClaveDTO(1L, null, null, null, null, "Ab3dE5gH"),
                new CandidatoClaveDTO(2L, null, null, null, null, "clave-invalida")
        ));

        EvaluacionCreadaDTO resultado = gestor.evaluarCandidatos(request);

        ArgumentCaptor<Map<Candidato, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(gestorCuestionario).generarCuestionariosParaEvaluacion(captor.capture(), eq(puesto), eq(7L));
        Map<Candidato, String> enviados = captor.getValue();
        assertThat(enviados.get(juan)).isEqualTo("Ab3dE5gH");
        assertThat(enviados.get(ana)).hasSize(8).isNotEqualTo("clave-invalida");

        assertThat(resultado.evaluacionId()).isEqualTo(55L);
        assertThat(resultado.puestoNombre()).isEqualTo("DESARROLLADOR BACKEND");
        assertThat(resultado.candidatos()).extracting(CandidatoClaveDTO::claveAcceso)
                .containsExactly("Ab3dE5gH", enviados.get(ana));
    }

    @Test
    @DisplayName("Finalizar revalida: si un candidato pasó a tener cuestionario activo no se guarda nada")
    void evaluarCandidatos_revalidaCuestionarioActivo() {
        Candidato juan = candidato(1, "PÉREZ", "JUAN");
        when(candidatoDAO.findByNroCandidatoAndEliminadoFalse(1L)).thenReturn(Optional.of(juan));
        when(cuestionarioDAO.tieneCuestionarioActivoOProceso(1L)).thenReturn(true);

        ConfirmarEvaluacionRequestDTO request = new ConfirmarEvaluacionRequestDTO("BACK-01",
                List.of(new CandidatoClaveDTO(1L, null, null, null, null, "Ab3dE5gH")));

        assertThatThrownBy(() -> gestor.evaluarCandidatos(request)).isInstanceOf(ReglaNegocioException.class);
        verify(gestorCuestionario, never()).generarCuestionariosParaEvaluacion(anyMap(), any(), anyLong());
    }

    // ----------------------------------------------------------- Excel

    @Test
    @DisplayName("Nombre del archivo Excel con formato aaaa-mm-dd-hh-mm-NombreFunción.xls")
    void nombreArchivo_formato() {
        String nombre = GestorCandidatos.nombreArchivo(LocalDateTime.of(2026, 11, 5, 9, 7), "ANALISTA QA/QC");
        assertThat(nombre).isEqualTo("2026-11-05-09-07-ANALISTA QAQC.xls");
    }

    @Test
    @DisplayName("Exportar evaluación inexistente lanza RecursoNoEncontradoException")
    void exportar_inexistente() {
        lenient().when(evaluacionDAO.obtenerPorId(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> gestor.exportarResultados(1L)).isInstanceOf(RecursoNoEncontradoException.class);
    }
}
