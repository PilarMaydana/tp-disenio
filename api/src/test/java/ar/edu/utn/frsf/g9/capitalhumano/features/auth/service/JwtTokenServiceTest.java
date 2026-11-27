package ar.edu.utn.frsf.g9.capitalhumano.features.auth.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.exception.AutenticacionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de la validación de tokens usada por el CU 026 para identificar al candidato.
 */
class JwtTokenServiceTest {

    /** La clave debe tener al menos 32 caracteres. */
    private static final String CLAVE = "clave-de-prueba-de-al-menos-32-caracteres";

    private final JwtTokenService servicio = new JwtTokenService(CLAVE, 3600, new ObjectMapper());

    @Test
    @DisplayName("Un token generado por el servicio es válido y conserva sus claims")
    void tokenValido() {
        String token = servicio.generarTokenCandidato("40111222", 4L, 9L);

        Map<String, Object> claims = servicio.validarToken(token);

        assertThat(claims).containsEntry("rol", "CANDIDATO");
        assertThat(((Number) claims.get("usuarioId")).longValue()).isEqualTo(4L);
    }

    @Test
    @DisplayName("Un token alterado se rechaza")
    void tokenAlterado() {
        String token = servicio.generarTokenCandidato("40111222", 4L, 9L);
        String[] partes = token.split("[.]");
        String otroPayload = servicio.generarTokenCandidato("99999999", 1L, 1L).split("[.]")[1];

        assertThatThrownBy(() -> servicio.validarToken(partes[0] + "." + otroPayload + "." + partes[2]))
                .isInstanceOf(AutenticacionException.class);
    }

    @Test
    @DisplayName("Un token vencido se rechaza")
    void tokenVencido() {
        JwtTokenService vencido = new JwtTokenService(CLAVE, -10, new ObjectMapper());
        String token = vencido.generarTokenCandidato("40111222", 4L, 9L);

        assertThatThrownBy(() -> servicio.validarToken(token))
                .isInstanceOf(AutenticacionException.class)
                .hasMessageContaining("expiró");
    }

    @Test
    @DisplayName("Sin token o con formato inválido se rechaza")
    void sinToken() {
        assertThatThrownBy(() -> servicio.validarToken(null)).isInstanceOf(AutenticacionException.class);
        assertThatThrownBy(() -> servicio.validarToken("abc")).isInstanceOf(AutenticacionException.class);
    }
}
