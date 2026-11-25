package ar.edu.utn.frsf.g9.capitalhumano.features.auth.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.exception.AutenticacionException;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Valida que el pedido provenga de un consultor autenticado (token JWT con rol CONSULTOR).
 *
 * <p>Lo usan las operaciones exclusivas del consultor, como el CU 025 - Evaluar a candidatos,
 * que expone datos personales de los candidatos y sus claves de ingreso.</p>
 */
@Service
public class SesionConsultorService {

    private static final String PREFIJO_BEARER = "Bearer ";
    private static final String ROL_CONSULTOR = "CONSULTOR";

    private final JwtTokenService jwtTokenService;

    public SesionConsultorService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Verifica que exista una sesión válida de consultor.
     *
     * @param authorizationHeader valor del header {@code Authorization} ("Bearer &lt;token&gt;")
     * @throws AutenticacionException si no hay token, es inválido o vencido, o no es de un consultor
     */
    public void verificarConsultor(String authorizationHeader) {
        String token = authorizationHeader != null && authorizationHeader.startsWith(PREFIJO_BEARER)
                ? authorizationHeader.substring(PREFIJO_BEARER.length())
                : null;
        Map<String, Object> claims = jwtTokenService.validarToken(token);
        if (!ROL_CONSULTOR.equals(claims.get("rol"))) {
            throw new AutenticacionException("Debe ingresar como consultor para realizar esta operación");
        }
    }
}
