package ar.edu.utn.frsf.g9.capitalhumano.features.puesto.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dto.PuestoBusquedaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dto.PuestoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.gestor.GestorPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.shared.web.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el alta y administración de Puestos (CU 009).
 */
@RestController
@RequestMapping("/api/puestos")
@Tag(name = "Puestos", description = "Endpoints para la administración de Puestos de trabajo (CU 009)")
public class PuestoController {

    private final GestorPuesto gestorPuesto;

    public PuestoController(GestorPuesto gestorPuesto) {
        this.gestorPuesto = gestorPuesto;
    }

    /**
     * Busca puestos vigentes por código, nombre y/o empresa (CU 025 - paso 8).
     * Sin filtros retorna todos los puestos vigentes.
     *
     * @param codigo  filtro de código
     * @param nombre  filtro de nombre
     * @param empresa filtro de empresa
     * @return lista de puestos
     */
    @GetMapping
    @Operation(summary = "Buscar puestos", description = "Busca puestos por código, nombre y/o empresa.")
    public ResponseEntity<List<PuestoBusquedaDTO>> buscar(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String empresa) {
        return ResponseEntity.ok(gestorPuesto.buscar(codigo, nombre, empresa));
    }

    /**
     * Obtiene un puesto con sus competencias y ponderaciones requeridas (CU 025 - paso 9).
     *
     * @param codigo código del puesto
     * @return puesto con sus características
     */
    @GetMapping("/{codigo}")
    @Operation(summary = "Obtener puesto", description = "Retorna el puesto con sus competencias y ponderaciones.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Puesto encontrado"),
            @ApiResponse(responseCode = "404", description = "Puesto inexistente",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PuestoDTO> obtener(@PathVariable String codigo) {
        return ResponseEntity.ok(gestorPuesto.obtener(codigo));
    }

    /**
     * Da de alta un nuevo puesto en el sistema con sus características requeridas.
     *
     * @param dto datos de entrada del puesto y sus competencias asociadas
     * @return DTO del puesto creado con HTTP 201
     */
    @PostMapping
    @Operation(summary = "Dar de alta un nuevo puesto", description = "Registra un nuevo puesto en el sistema asociando las características y competencias evaluables.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Puesto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada incompletos o inválidos (ponderación fuera de rango o campos vacíos)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Código o nombre de puesto ya registrado en el sistema",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio violada (ej. competencias duplicadas en el puesto)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PuestoDTO> crearPuesto(@Valid @RequestBody PuestoDTO dto) {
        PuestoDTO creado = gestorPuesto.crearPuesto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}

