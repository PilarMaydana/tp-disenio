package ar.edu.utn.frsf.g9.capitalhumano.shared.security;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.service.JwtTokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Provee el identificador del usuario autenticado a la auditoría del dominio. */
@Component
public class JwtContextoSeguridad implements ContextoSeguridad {

    @Override
    public Long getUsuarioActualId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtTokenService.TokenClaims claims)) {
            throw new IllegalStateException("No hay un usuario autenticado en el contexto de seguridad");
        }
        return claims.usuarioId();
    }
}
