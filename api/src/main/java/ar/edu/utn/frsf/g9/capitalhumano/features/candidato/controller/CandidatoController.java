package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dto.CandidatoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.gestor.GestorCandidatos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la búsqueda de Candidatos (CU 025 - pasos 2 y 3).
 */
@RestController
@RequestMapping("/api/candidatos")
@Tag(name = "Candidatos", description = "Búsqueda de candidatos (CU 025)")
public class CandidatoController {

    private final GestorCandidatos gestorCandidatos;

    public CandidatoController(GestorCandidatos gestorCandidatos) {
        this.gestorCandidatos = gestorCandidatos;
    }

    /**
     * Busca candidatos por apellido, nombre y/o número de candidato.
     * Sin filtros retorna todos los candidatos vigentes.
     *
     * @param apellido     filtro de apellido (parcial, ignora acentos)
     * @param nombre       filtro de nombre (parcial, ignora acentos)
     * @param nroCandidato filtro de número de candidato (exacto)
     * @return candidatos encontrados
     */
    @GetMapping
    @Operation(summary = "Buscar candidatos", description = "Busca candidatos por apellido, nombre y/o número de candidato.")
    public ResponseEntity<List<CandidatoDTO>> buscarCandidatos(
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String nroCandidato) {
        Long nro = null;
        if (nroCandidato != null && !nroCandidato.isBlank()) {
            try {
                nro = Long.parseLong(nroCandidato.trim());
            } catch (NumberFormatException e) {
                // Un número de candidato no numérico no puede coincidir con ningún candidato
                return ResponseEntity.ok(List.of());
            }
        }
        return ResponseEntity.ok(gestorCandidatos.buscarCandidatos(apellido, nombre, nro));
    }
}
