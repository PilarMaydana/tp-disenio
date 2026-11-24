package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.service.SesionCandidatoService;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioResumenDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioWizardDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.InstruccionesDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.ResponderBloqueRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GestorCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del CU 026 - Completar cuestionario.
 *
 * <p>El candidato se identifica con el token obtenido al ingresar (CU 001); sólo puede acceder
 * a su propio cuestionario.</p>
 */
@RestController
@RequestMapping("/api/cuestionarios")
@Tag(name = "Cuestionarios", description = "Completar cuestionario por parte del candidato (CU 026)")
public class CuestionarioController {

    private final GestorCuestionario gestorCuestionario;
    private final SesionCandidatoService sesionCandidatoService;

    public CuestionarioController(GestorCuestionario gestorCuestionario,
                                  SesionCandidatoService sesionCandidatoService) {
        this.gestorCuestionario = gestorCuestionario;
        this.sesionCandidatoService = sesionCandidatoService;
    }

    /**
     * Instrucciones del cuestionario (mensajes 2 y 3).
     *
     * @return instrucciones y parámetros
     */
    @GetMapping("/instrucciones")
    @Operation(summary = "Obtener instrucciones", description = "Texto y parámetros de la pantalla 'Cuestionario - Instrucciones'.")
    public ResponseEntity<InstruccionesDTO> obtenerInstrucciones() {
        return ResponseEntity.ok(gestorCuestionario.obtenerInstrucciones());
    }

    /**
     * Cuestionario abierto del candidato autenticado.
     *
     * @param authorization token del candidato
     * @return resumen del cuestionario
     */
    @GetMapping("/actual")
    @Operation(summary = "Cuestionario del candidato", description = "Datos del cuestionario ACTIVO o EN_PROCESO del candidato autenticado.")
    public ResponseEntity<CuestionarioResumenDTO> obtenerActual(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        Long nroCandidato = sesionCandidatoService.obtenerNroCandidato(authorization);
        return ResponseEntity.ok(gestorCuestionario.obtenerResumen(nroCandidato));
    }

    /**
     * El candidato acepta las instrucciones: inicia o reanuda el cuestionario (mensaje 8).
     *
     * @param authorization token del candidato
     * @return bloque pendiente del wizard
     */
    @PostMapping("/actual/iniciar")
    @Operation(summary = "Iniciar o reanudar el cuestionario",
            description = "Primer ingreso: elige las preguntas al azar y arma los bloques. Reingreso (1.C): se posiciona en el bloque pendiente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bloque pendiente del cuestionario"),
            @ApiResponse(responseCode = "401", description = "Sin sesión de candidato",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "El candidato no tiene un cuestionario activo o en proceso",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Tiempo o accesos superados (1.A / 1.B)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CuestionarioWizardDTO> iniciarOReanudar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        Long nroCandidato = sesionCandidatoService.obtenerNroCandidato(authorization);
        return ResponseEntity.ok(gestorCuestionario.iniciarOReanudarCuestionario(nroCandidato));
    }

    /**
     * Guarda las respuestas de un bloque (mensaje 47).
     *
     * @param authorization token del candidato
     * @param id            cuestionario
     * @param numero        número de bloque
     * @param request       respuestas del bloque
     * @return siguiente bloque, o el cuestionario completado
     */
    @PostMapping("/{id}/bloques/{numero}")
    @Operation(summary = "Responder un bloque",
            description = "Valida que estén todas las preguntas respondidas (6.A). En el último bloque calcula los resultados y completa el cuestionario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Siguiente bloque o cuestionario completado"),
            @ApiResponse(responseCode = "422", description = "Preguntas sin responder (6.A) o tiempo superado (1.A)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CuestionarioWizardDTO> responderBloque(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id,
            @PathVariable Integer numero,
            @Valid @RequestBody ResponderBloqueRequestDTO request) {
        Long nroCandidato = sesionCandidatoService.obtenerNroCandidato(authorization);
        return ResponseEntity.ok(gestorCuestionario.responderBloque(nroCandidato, id, numero, request.respuestas()));
    }
}
