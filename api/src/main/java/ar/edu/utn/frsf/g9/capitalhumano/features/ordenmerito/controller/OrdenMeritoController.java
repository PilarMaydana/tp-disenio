package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.controller;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dao.CuestionarioOrdenMeritoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dao.ResultadoCompetenciaDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.CandidatoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.EvaluacionOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.InformeOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.PuestoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.exportacion.ArchivoInformeOrdenMerito;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.exportacion.ExportadorInformeOrdenMerito;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.exportacion.FormatoInformeOrdenMerito;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.model.ResultadoCompetencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.service.ClasificadorMerito;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Consultas de puestos, cuestionarios y resultados para el CU 028. */
@RestController
@RequestMapping("/api/ordenes-merito")
@Tag(name = "Orden de mérito", description = "Consultas para emitir la orden de mérito (CU 028)")
public class OrdenMeritoController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es-AR"));
    private final PuestoDAO puestoDAO;
    private final CuestionarioOrdenMeritoDAO cuestionarioDAO;
    private final ResultadoCompetenciaDAO resultadoDAO;
    private final ExportadorInformeOrdenMerito exportadorInforme;
    private final ClasificadorMerito clasificadorMerito;

    public OrdenMeritoController(PuestoDAO puestoDAO, CuestionarioOrdenMeritoDAO cuestionarioDAO,
                                 ResultadoCompetenciaDAO resultadoDAO, ExportadorInformeOrdenMerito exportadorInforme,
                                 ClasificadorMerito clasificadorMerito) {
        this.puestoDAO = puestoDAO;
        this.cuestionarioDAO = cuestionarioDAO;
        this.resultadoDAO = resultadoDAO;
        this.exportadorInforme = exportadorInforme;
        this.clasificadorMerito = clasificadorMerito;
    }

    @GetMapping("/puestos")
    @Transactional(readOnly = true)
    @Operation(summary = "Buscar puestos para emitir orden de mérito")
    public List<PuestoOrdenMeritoDTO> buscarPuestos(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombrePuesto,
            @RequestParam(required = false) String empresa) {
        return puestoDAO.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getEliminado()))
                .filter(p -> contiene(p.getCodigo(), codigo))
                .filter(p -> contiene(p.getNombre(), nombrePuesto))
                .filter(p -> contiene(p.getEmpresa(), empresa))
                .map(p -> {
                    List<Cuestionario> cuestionarios = cuestionarioDAO.findByPuestoIdAndEliminadoFalseOrderByIdAsc(p.getId());
                    return new PuestoOrdenMeritoDTO(p.getId(), p.getCodigo(), p.getNombre(), p.getEmpresa(), p.getEmpresa(),
                            p.getDescripcion(), cuestionarios.size(), cuestionarios.stream()
                            .filter(c -> c.getEstado() != null && c.getEstado().name().equals("COMPLETO")).count());
                }).toList();
    }

    @GetMapping("/empresas")
    @Transactional(readOnly = true)
    @Operation(summary = "Listar empresas con puestos registrados")
    public List<String> listarEmpresas() {
        return puestoDAO.findAll().stream().filter(p -> !Boolean.TRUE.equals(p.getEliminado()))
                .map(Puesto::getEmpresa).distinct().sorted().toList();
    }

    @GetMapping("/puestos/{puestoId}/evaluaciones")
    @Transactional(readOnly = true)
    @Operation(summary = "Listar cuestionarios asociados al puesto")
    public List<EvaluacionOrdenMeritoDTO> listarEvaluaciones(@PathVariable Long puestoId) {
        validarPuesto(puestoId);
        return cuestionarioDAO.findByPuestoIdAndEliminadoFalseOrderByIdAsc(puestoId).stream()
                .map(c -> {
                    LocalDateTime fechaEvaluacion = c.getFechaInicio() != null ? c.getFechaInicio() : c.getFechaCreacion();
                    return new EvaluacionOrdenMeritoDTO(c.getId(), fecha(fechaEvaluacion), fechaISO(fechaEvaluacion),
                            "Cuestionario de " + c.getCandidato().getApellido() + ", " + c.getCandidato().getNombre(),
                            c.getEstado() != null && c.getEstado().name().equals("COMPLETO") ? "Completada" : "Parcial");
                })
                .toList();
    }

    @GetMapping("/puestos/{puestoId}/reporte")
    @Transactional(readOnly = true)
    @Operation(summary = "Obtener candidatos de las evaluaciones seleccionadas")
    public List<CandidatoOrdenMeritoDTO> obtenerReporte(@PathVariable Long puestoId,
            @RequestParam List<Long> evaluacionIds) {
        Puesto puesto = validarPuesto(puestoId);
        List<Cuestionario> cuestionarios = cuestionarioDAO.findByPuestoIdAndEliminadoFalseOrderByIdAsc(puestoId).stream()
                .filter(c -> evaluacionIds.contains(c.getId())).toList();
        Set<Long> idsEncontrados = cuestionarios.stream().map(Cuestionario::getId).collect(Collectors.toSet());
        if (idsEncontrados.size() != evaluacionIds.stream().distinct().count()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una o más evaluaciones no pertenecen al puesto seleccionado");
        }

        Map<Long, Map<Long, Double>> puntajes = resultadoDAO.findByCuestionarioIdIn(cuestionarios.stream()
                        .map(Cuestionario::getId).toList()).stream()
                .collect(Collectors.groupingBy(r -> r.getCuestionario().getId(),
                        Collectors.toMap(r -> r.getCompetencia().getId(), ResultadoCompetencia::getPuntaje)));

        return clasificadorMerito.clasificar(cuestionarios, puesto, puntajes);
    }

    @GetMapping("/puestos/{puestoId}/reporte/descargar")
    @Transactional(readOnly = true)
    @Operation(summary = "Descargar el informe de orden de mérito en Excel o Word, o generar PDF para impresión")
    public ResponseEntity<byte[]> descargarReporte(@PathVariable Long puestoId,
            @RequestParam List<Long> evaluacionIds,
            @RequestParam FormatoInformeOrdenMerito formato) throws java.io.IOException {
        Puesto puesto = validarPuesto(puestoId);
        List<EvaluacionOrdenMeritoDTO> evaluacionesDisponibles = listarEvaluaciones(puestoId);
        Set<Long> idsSolicitados = new HashSet<>(evaluacionIds);
        List<EvaluacionOrdenMeritoDTO> evaluaciones = evaluacionesDisponibles.stream()
                .filter(e -> idsSolicitados.contains(e.id())).toList();
        if (evaluaciones.isEmpty() || evaluaciones.size() != idsSolicitados.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seleccioná evaluaciones asociadas al puesto");
        }

        List<CandidatoOrdenMeritoDTO> candidatos = obtenerReporte(puestoId, evaluacionIds);
        InformeOrdenMeritoDTO informe = new InformeOrdenMeritoDTO(puesto.getCodigo(), puesto.getNombre(),
                puesto.getEmpresa(), puesto.getDescripcion(), LocalDateTime.now(), evaluaciones, candidatos);
        ArchivoInformeOrdenMerito archivo = exportadorInforme.exportar(informe, formato);
        String nombreArchivo = "orden-merito-" + puesto.getCodigo().replaceAll("[^A-Za-z0-9_-]", "_")
                + "." + archivo.extension();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(nombreArchivo, StandardCharsets.UTF_8).build().toString())
                .body(archivo.contenido());
    }

    private Puesto validarPuesto(Long puestoId) {
        return puestoDAO.findById(puestoId).filter(p -> !Boolean.TRUE.equals(p.getEliminado()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Puesto", puestoId));
    }

    private boolean contiene(String valor, String criterio) {
        return criterio == null || criterio.isBlank() || (valor != null && valor.toLowerCase(Locale.ROOT).contains(criterio.trim().toLowerCase(Locale.ROOT)));
    }

    private String fecha(LocalDateTime fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    private String fechaISO(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.toLocalDate().toString();
    }
}
