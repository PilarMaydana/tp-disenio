package ar.edu.utn.frsf.g9.capitalhumano.features.auth.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.dao.CandidatoRepository;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.exception.AutenticacionException;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Identifica al candidato autenticado a partir del token que obtuvo en el CU 001.
 * Lo usa el CU 026 para que cada candidato sólo pueda acceder a su propio cuestionario.
 */
@Service
public class SesionCandidatoService {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final CandidatoRepository candidatoRepository;

    public SesionCandidatoService(JwtTokenService jwtTokenService, CandidatoRepository candidatoRepository) {
        this.jwtTokenService = jwtTokenService;
        this.candidatoRepository = candidatoRepository;
    }

    /**
     * Devuelve el número del candidato autenticado.
     *
     * @param authorizationHeader valor del header {@code Authorization} ("Bearer &lt;token&gt;")
     * @return número de candidato
     * @throws AutenticacionException si no hay sesión válida de candidato
     */
    @Transactional(readOnly = true)
    public Long obtenerNroCandidato(String authorizationHeader) {
        String token = authorizationHeader != null && authorizationHeader.startsWith(PREFIJO_BEARER)
                ? authorizationHeader.substring(PREFIJO_BEARER.length())
                : null;
        Map<String, Object> claims = jwtTokenService.validarToken(token);
        if (!"CANDIDATO".equals(claims.get("rol")) || !(claims.get("usuarioId") instanceof Number id)) {
            throw new AutenticacionException("Debe ingresar como candidato para completar el cuestionario");
        }
        return candidatoRepository.findById(id.longValue())
                .map(Candidato::getNroCandidato)
                .orElseThrow(() -> new AutenticacionException("Sesión inválida"));
    }
}
