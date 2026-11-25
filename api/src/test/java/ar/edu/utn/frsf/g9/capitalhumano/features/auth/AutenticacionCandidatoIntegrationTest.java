package ar.edu.utn.frsf.g9.capitalhumano.features.auth;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dto.CandidatoLoginRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.exception.AutenticacionException;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.service.AuthService;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dao.CandidatoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CU 001 (ingreso del candidato al CU 026) contra la base H2 del seed: un candidato o un
 * cuestionario dado de baja lógica no puede iniciar sesión.
 */
@SpringBootTest
@Transactional
class AutenticacionCandidatoIntegrationTest {

    /** SUÁREZ, del seed: cuestionario ACTIVO con clave Suarez16. */
    private static final long NRO_SUAREZ = 16L;
    private static final CandidatoLoginRequestDTO LOGIN_SUAREZ =
            new CandidatoLoginRequestDTO(TipoDocumento.DNI, "35111008", "Suarez16");

    @Autowired
    private AuthService authService;

    @Autowired
    private CandidatoDAO candidatoDAO;

    @Autowired
    private CuestionarioDAO cuestionarioDAO;

    @Test
    @DisplayName("Candidato vigente con cuestionario activo inicia sesión")
    void candidatoVigente() {
        assertThat(authService.autenticarCandidato(LOGIN_SUAREZ).getToken()).isNotBlank();
    }

    @Test
    @DisplayName("Candidato dado de baja lógica no puede iniciar sesión")
    void candidatoDadoDeBaja() {
        candidatoDAO.findByNroCandidatoAndEliminadoFalse(NRO_SUAREZ).orElseThrow().setEliminado(true);

        assertThatThrownBy(() -> authService.autenticarCandidato(LOGIN_SUAREZ))
                .isInstanceOf(AutenticacionException.class);
    }

    @Test
    @DisplayName("Cuestionario dado de baja lógica no permite iniciar sesión")
    void cuestionarioDadoDeBaja() {
        cuestionarioDAO.obtenerActivoOEnProceso(NRO_SUAREZ).orElseThrow().setEliminado(true);

        assertThatThrownBy(() -> authService.autenticarCandidato(LOGIN_SUAREZ))
                .isInstanceOf(AutenticacionException.class);
    }
}
