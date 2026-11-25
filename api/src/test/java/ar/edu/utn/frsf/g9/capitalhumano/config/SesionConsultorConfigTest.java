package ar.edu.utn.frsf.g9.capitalhumano.config;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los endpoints del CU 025 exigen sesión de consultor (TC-025-02 de la auditoría de QA).
 */
@SpringBootTest
@Transactional
class SesionConsultorConfigTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    /** MockMvc sobre el mismo contexto que el resto de los tests (no crea otro contexto ni otra base). */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    private String bearerConsultor() {
        return "Bearer " + jwtTokenService.generarTokenConsultor("LFERNANDEZ", 1L);
    }

    private String bearerCandidato() {
        return "Bearer " + jwtTokenService.generarTokenCandidato("35111008", 16L, 12L);
    }

    @Test
    @DisplayName("Sin token: buscar candidatos y descargar el Excel responden 401")
    void sinToken() throws Exception {
        mockMvc.perform(get("/api/candidatos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("AUTENTICACION_FALLIDA"));
        mockMvc.perform(get("/api/evaluaciones/1/excel"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token de candidato: no puede crear evaluaciones (401)")
    void tokenDeCandidato() throws Exception {
        mockMvc.perform(post("/api/evaluaciones")
                        .header(HttpHeaders.AUTHORIZATION, bearerCandidato())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puestoCodigo\":\"BACK-01\",\"candidatos\":[{\"nroCandidato\":4}]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Debe ingresar como consultor para realizar esta operación"));
    }

    @Test
    @DisplayName("Token inválido: 401")
    void tokenInvalido() throws Exception {
        mockMvc.perform(get("/api/candidatos").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token de consultor: accede normalmente")
    void tokenDeConsultor() throws Exception {
        mockMvc.perform(get("/api/candidatos").header(HttpHeaders.AUTHORIZATION, bearerConsultor()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/evaluaciones/1/excel").header(HttpHeaders.AUTHORIZATION, bearerConsultor()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("El preflight de CORS (sin token) no se bloquea")
    void preflightCors() throws Exception {
        mockMvc.perform(options("/api/evaluaciones")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
    }

    @Test
    @DisplayName("Los endpoints del candidato (CU 026) no exigen sesión de consultor")
    void endpointsDelCandidatoNoAfectados() throws Exception {
        mockMvc.perform(get("/api/cuestionarios/instrucciones"))
                .andExpect(status().isOk());
    }
}
