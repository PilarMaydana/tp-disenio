package ar.edu.utn.frsf.g9.capitalhumano;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.service.JwtTokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * Simula en los tests a la consultora autenticada (como lo hace el filtro JWT en la app),
 * necesario para las operaciones que registran el usuario actual (por ejemplo, crear una evaluación).
 */
public final class ConsultorAutenticadoEnTest {

    private ConsultorAutenticadoEnTest() {
    }

    /** Autentica a la consultora en el hilo actual. */
    public static void autenticar() {
        JwtTokenService.TokenClaims claims = new JwtTokenService.TokenClaims("LFERNANDEZ", "CONSULTOR", 1L, null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(claims, null, List.of()));
    }

    /** Limpia la autenticación del hilo actual. */
    public static void limpiar() {
        SecurityContextHolder.clearContext();
    }
}
