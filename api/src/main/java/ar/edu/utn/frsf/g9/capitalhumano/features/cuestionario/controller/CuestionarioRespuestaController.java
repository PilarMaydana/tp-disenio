package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.GuardarCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioRespuestaResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.GuardarRespuestasDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.PreguntaRespuestaCuestionarioDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.service.CuestionarioRespuestaService;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.service.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/cuestionarios")
public class CuestionarioRespuestaController {

    private final CuestionarioRespuestaService service;
    private final JwtTokenService jwtTokenService;

    public CuestionarioRespuestaController(CuestionarioRespuestaService service, JwtTokenService jwtTokenService) {
        this.service = service;
        this.jwtTokenService = jwtTokenService;
    }

    @GetMapping("/{id}/resumen")
    public CuestionarioRespuestaResumenDTO obtenerResumen(@PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        validarToken(authorization, id);
        return service.obtenerResumen(id);
    }

    @GetMapping("/{id}/preguntas")
    public List<PreguntaRespuestaCuestionarioDTO> obtenerPreguntas(@PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        validarToken(authorization, id);
        return service.obtenerPreguntas(id);
    }

    @PostMapping("/{id}/respuestas")
    public GuardarCuestionarioDTO guardarRespuestas(@PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody GuardarRespuestasDTO request) {
        validarToken(authorization, id);
        return service.guardarRespuestas(id, request);
    }

    private void validarToken(String authorization, Long cuestionarioId) {
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)
                || !jwtTokenService.esTokenValidoParaCuestionario(authorization.substring(7).trim(), cuestionarioId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión no corresponde a este cuestionario");
        }
    }
}
