package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.gestor.GestorCandidatos;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ArchivoExcelDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CandidatoClaveDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ConfirmarEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluacionCreadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluarCandidatosRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ValidacionPuestoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de {@link EvaluacionController} con MockMvc (CU 025).
 */
@ExtendWith(MockitoExtension.class)
class EvaluacionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GestorCandidatos gestorCandidatos;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new EvaluacionController(gestorCandidatos))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /validar-candidatos con candidato activo retorna 422 con el mensaje del flujo 6.A")
    void validarCandidatos_422() throws Exception {
        when(gestorCandidatos.verificarCandidatosDisponibles(any()))
                .thenThrow(new ReglaNegocioException("El siguiente candidato posee un cuestionario activo o en proceso"));

        mockMvc.perform(post("/api/evaluaciones/validar-candidatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nroCandidatos\":[1]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje", containsString("cuestionario activo")));
    }

    @Test
    @DisplayName("GET /puestos/{codigo}/validacion retorna las competencias no evaluables")
    void validarPuesto_ok() throws Exception {
        when(gestorCandidatos.validarCompetenciasEvaluables("DEV-01"))
                .thenReturn(new ValidacionPuestoDTO("DEV-01", false, List.of("TRABAJO EN EQUIPO")));

        mockMvc.perform(get("/api/evaluaciones/puestos/DEV-01/validacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluable").value(false))
                .andExpect(jsonPath("$.competenciasNoEvaluables[0]").value("TRABAJO EN EQUIPO"));
    }

    @Test
    @DisplayName("POST /preparar retorna las claves generadas")
    void preparar_ok() throws Exception {
        when(gestorCandidatos.prepararEvaluacion(any(EvaluarCandidatosRequestDTO.class))).thenReturn(List.of(
                new CandidatoClaveDTO(4L, "ROMERO", "AGUSTÍN", TipoDocumento.DNI, "40111222", "Ab3dE5gH")));

        mockMvc.perform(post("/api/evaluaciones/preparar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nroCandidatos\":[4],\"puestoCodigo\":\"BACK-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].claveAcceso").value("Ab3dE5gH"))
                .andExpect(jsonPath("$[0].tipoDocumento").value("DNI"));
    }

    @Test
    @DisplayName("POST /api/evaluaciones retorna 201 con la evaluación creada")
    void evaluar_201() throws Exception {
        when(gestorCandidatos.evaluarCandidatos(any(ConfirmarEvaluacionRequestDTO.class))).thenReturn(
                new EvaluacionCreadaDTO(9L, "BACK-01", "DESARROLLADOR BACKEND JUNIOR", "TECH SOLUTIONS",
                        LocalDateTime.of(2026, 11, 19, 17, 30), List.of()));

        mockMvc.perform(post("/api/evaluaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puestoCodigo\":\"BACK-01\",\"candidatos\":[{\"nroCandidato\":4,\"claveAcceso\":\"Ab3dE5gH\"}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.evaluacionId").value(9));
    }

    @Test
    @DisplayName("POST /api/evaluaciones sin número de candidato retorna 400")
    void evaluar_400() throws Exception {
        mockMvc.perform(post("/api/evaluaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puestoCodigo\":\"BACK-01\",\"candidatos\":[{\"claveAcceso\":\"Ab3dE5gH\"}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /{id}/excel descarga el archivo .xls con el nombre en Content-Disposition")
    void exportarExcel_ok() throws Exception {
        byte[] bytes = {1, 2, 3};
        when(gestorCandidatos.exportarResultados(9L))
                .thenReturn(new ArchivoExcelDTO("2026-11-19-17-30-ANALISTA QA.xls", bytes));

        mockMvc.perform(get("/api/evaluaciones/9/excel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.ms-excel"))
                .andExpect(header().string("Content-Disposition", containsString(".xls")))
                .andExpect(content().bytes(bytes));
    }
}
