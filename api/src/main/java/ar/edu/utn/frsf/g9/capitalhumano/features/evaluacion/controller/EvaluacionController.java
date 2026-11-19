package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dto.CandidatoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.gestor.GestorCandidatos;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ArchivoExcelDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CandidatoClaveDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ConfirmarEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluacionCreadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluarCandidatosRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ValidacionPuestoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controlador REST del CU 025 - Evaluar a Candidatos.
 */
@RestController
@RequestMapping("/api/evaluaciones")
@Tag(name = "Evaluaciones", description = "Evaluación de candidatos en un puesto (CU 025)")
public class EvaluacionController {

    /** Tipo MIME de los archivos Excel 97-2003 (.xls). */
    static final MediaType EXCEL_XLS = MediaType.parseMediaType("application/vnd.ms-excel");

    private final GestorCandidatos gestorCandidatos;

    public EvaluacionController(GestorCandidatos gestorCandidatos) {
        this.gestorCandidatos = gestorCandidatos;
    }

    /**
     * Verifica que los candidatos seleccionados puedan ser evaluados (paso 6 / flujos 4.A y 6.A).
     *
     * @param request números de candidato seleccionados
     * @return candidatos validados
     */
    @PostMapping("/validar-candidatos")
    @Operation(summary = "Validar candidatos a evaluar",
            description = "Verifica que se haya seleccionado al menos un candidato y que ninguno tenga cuestionarios activos o en proceso.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todos los candidatos pueden ser evaluados"),
            @ApiResponse(responseCode = "404", description = "Algún candidato no existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "No se seleccionaron candidatos o alguno tiene cuestionarios activos/en proceso",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<CandidatoDTO>> validarCandidatos(@RequestBody EvaluarCandidatosRequestDTO request) {
        return ResponseEntity.ok(gestorCandidatos.verificarCandidatosDisponibles(request.nroCandidatos()));
    }

    /**
     * Valida que todas las competencias del puesto puedan evaluarse (paso 9 / flujo 9.A).
     *
     * @param codigo código del puesto
     * @return resultado de la validación
     */
    @GetMapping("/puestos/{codigo}/validacion")
    @Operation(summary = "Validar competencias evaluables del puesto",
            description = "Indica qué competencias del puesto no poseen al menos un factor con 2 o más preguntas.")
    public ResponseEntity<ValidacionPuestoDTO> validarPuesto(@PathVariable String codigo) {
        return ResponseEntity.ok(gestorCandidatos.validarCompetenciasEvaluables(codigo));
    }

    /**
     * Genera las claves de ingreso para mostrarlas al consultor antes de finalizar (paso 11).
     * No persiste datos.
     *
     * @param request candidatos y puesto
     * @return candidatos con su clave de ingreso
     */
    @PostMapping("/preparar")
    @Operation(summary = "Preparar evaluación",
            description = "Valida candidatos y puesto y genera las claves de ingreso. No guarda datos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claves generadas"),
            @ApiResponse(responseCode = "422", description = "Regla de negocio violada (4.A, 6.A, 9.A o 10.A)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<CandidatoClaveDTO>> prepararEvaluacion(@RequestBody EvaluarCandidatosRequestDTO request) {
        return ResponseEntity.ok(gestorCandidatos.prepararEvaluacion(request));
    }

    /**
     * Guarda la evaluación con un cuestionario ACTIVO por candidato (paso 12 - Finalizar).
     *
     * @param request puesto y candidatos con las claves mostradas
     * @return evaluación creada con HTTP 201
     */
    @PostMapping
    @Operation(summary = "Evaluar candidatos",
            description = "Crea la evaluación y un cuestionario en estado ACTIVO para cada candidato.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evaluación creada"),
            @ApiResponse(responseCode = "404", description = "Candidato o puesto inexistente",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio violada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EvaluacionCreadaDTO> evaluarCandidatos(@Valid @RequestBody ConfirmarEvaluacionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestorCandidatos.evaluarCandidatos(request));
    }

    /**
     * Descarga el Excel con los candidatos de la evaluación y sus claves (pasos 14 a 17).
     *
     * @param id identificador de la evaluación
     * @return archivo .xls
     */
    @GetMapping("/{id}/excel")
    @Operation(summary = "Exportar candidatos y claves a Excel",
            description = "Genera el archivo aaaa-mm-dd-hh-mm-NombreFunción.xls de la evaluación.")
    public ResponseEntity<byte[]> exportarExcel(@PathVariable Long id) {
        ArchivoExcelDTO archivo = gestorCandidatos.exportarResultados(id);
        ContentDisposition disposicion = ContentDisposition.attachment()
                .filename(archivo.nombreArchivo(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .contentType(EXCEL_XLS)
                .body(archivo.contenido());
    }
}
