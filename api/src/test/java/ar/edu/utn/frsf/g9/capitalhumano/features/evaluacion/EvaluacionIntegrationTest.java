package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dto.CandidatoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.gestor.GestorCandidatos;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ArchivoExcelDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CandidatoClaveDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ConfirmarEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluacionCreadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluarCandidatosRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.gestor.GestorPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import ar.edu.utn.frsf.g9.capitalhumano.ConsultorAutenticadoEnTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integración del CU 025 contra la base H2 cargada con {@code seed.sql}.
 */
@SpringBootTest
@Transactional
class EvaluacionIntegrationTest {

    @Autowired
    private GestorCandidatos gestorCandidatos;

    @Autowired
    private GestorPuesto gestorPuesto;

    @Autowired
    private CuestionarioDAO cuestionarioDAO;

    /** Candidatos del seed sin cuestionarios (CU 025). */
    private static final long ROMERO = 19L;
    private static final long DIAZ = 20L;

    @BeforeEach
    void autenticar() {
        ConsultorAutenticadoEnTest.autenticar();
    }

    @AfterEach
    void limpiar() {
        ConsultorAutenticadoEnTest.limpiar();
    }

    @Test
    @DisplayName("Búsqueda de candidatos ignora mayúsculas y acentos")
    void buscarCandidatos_ignoraAcentos() {
        List<CandidatoDTO> resultado = gestorCandidatos.buscarCandidatos("romero", null, null);
        assertThat(resultado).extracting(CandidatoDTO::nombre).containsExactlyInAnyOrder("AGUSTÍN", "LUCAS");

        assertThat(gestorCandidatos.buscarCandidatos("perez", "juan", null)).hasSize(1);
        assertThat(gestorCandidatos.buscarCandidatos(null, null, DIAZ))
                .singleElement().extracting(CandidatoDTO::apellido).isEqualTo("DÍAZ");
    }

    @Test
    @DisplayName("Puestos del seed: BACK-01 es evaluable y QA-01 no (9.A)")
    void validarPuestosDelSeed() {
        assertThat(gestorPuesto.buscar(null, null, null)).extracting(p -> p.codigo())
                .contains("BACK-01", "DEV-01", "QA-01");

        assertThat(gestorCandidatos.validarCompetenciasEvaluables("BACK-01").evaluable()).isTrue();

        // QA-01 requiere RESOLUCIÓN DE PROBLEMAS, que no tiene factores con preguntas
        var qa = gestorCandidatos.validarCompetenciasEvaluables("QA-01");
        assertThat(qa.evaluable()).isFalse();
        assertThat(qa.competenciasNoEvaluables()).containsExactly("RESOLUCIÓN DE PROBLEMAS");
    }

    @Test
    @DisplayName("6.A: candidatos del seed con cuestionario ACTIVO / EN_PROCESO no pueden evaluarse")
    void candidatosConCuestionarioActivo() {
        // PÉREZ (1) tiene un cuestionario ACTIVO; LÓPEZ (3) lo tiene COMPLETO
        assertThatThrownBy(() -> gestorCandidatos.verificarCandidatosDisponibles(List.of(1L, 3L, ROMERO)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("PÉREZ, JUAN")
                .hasMessageNotContaining("LÓPEZ");

        // El candidato 3 tiene su cuestionario COMPLETO: puede volver a evaluarse
        assertThat(gestorCandidatos.verificarCandidatosDisponibles(List.of(3L, ROMERO))).hasSize(2);
    }

    @Test
    @DisplayName("Flujo completo: preparar, finalizar, cuestionarios ACTIVOS y exportación a Excel")
    void flujoCompleto() throws Exception {
        List<CandidatoClaveDTO> preparados = gestorCandidatos.prepararEvaluacion(
                new EvaluarCandidatosRequestDTO(List.of(ROMERO, DIAZ), "BACK-01"));
        assertThat(preparados).hasSize(2);

        EvaluacionCreadaDTO creada = gestorCandidatos.evaluarCandidatos(
                new ConfirmarEvaluacionRequestDTO("BACK-01", preparados));

        assertThat(creada.evaluacionId()).isNotNull();
        assertThat(creada.candidatos()).extracting(CandidatoClaveDTO::claveAcceso)
                .containsExactlyElementsOf(preparados.stream().map(CandidatoClaveDTO::claveAcceso).toList());

        List<Cuestionario> cuestionarios = cuestionarioDAO.findAll().stream()
                .filter(q -> q.getEvaluacion() != null && creada.evaluacionId().equals(q.getEvaluacion().getId()))
                .toList();
        assertThat(cuestionarios).hasSize(2)
                .allSatisfy(q -> assertThat(q.getEstado()).isEqualTo(EstadoCuestionario.ACTIVO));

        // Ahora los candidatos tienen cuestionario activo: no pueden evaluarse de nuevo (6.A)
        assertThatThrownBy(() -> gestorCandidatos.verificarCandidatosDisponibles(List.of(ROMERO)))
                .isInstanceOf(ReglaNegocioException.class);

        ArchivoExcelDTO excel = gestorCandidatos.exportarResultados(creada.evaluacionId());
        assertThat(excel.nombreArchivo()).matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-DESARROLLADOR BACKEND JUNIOR\\.xls");

        try (HSSFWorkbook libro = new HSSFWorkbook(new ByteArrayInputStream(excel.contenido()))) {
            Sheet hoja = libro.getSheetAt(0);
            assertThat(hoja.getRow(5).getCell(4).getStringCellValue()).isEqualTo("Clave de ingreso");
            assertThat(hoja.getRow(6).getCell(0).getStringCellValue()).isEqualTo("ROMERO");
            assertThat(hoja.getRow(6).getCell(4).getStringCellValue()).isEqualTo(preparados.get(0).claveAcceso());
            assertThat(hoja.getRow(7).getCell(0).getStringCellValue()).isEqualTo("DÍAZ");
        }
    }
}
