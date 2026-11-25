package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dao.CandidatoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dto.CandidatoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GestorCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao.EvaluacionDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ArchivoExcelDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CandidatoClaveDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ConfirmarEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluacionCreadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluarCandidatosRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ValidacionPuestoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.dao.FactorDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.dao.PuestoDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.RecursoNoEncontradoException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import ar.edu.utn.frsf.g9.capitalhumano.shared.security.ContextoSeguridad;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Gestor de lógica de negocio de Candidatos. Implementa el CU 025 - Evaluar a Candidatos.
 *
 * <p>Los nombres de los métodos siguen el diagrama de secuencia del CU 025.</p>
 */
@Service
public class GestorCandidatos {

    private static final Logger log = LoggerFactory.getLogger(GestorCandidatos.class);

    /** Caracteres válidos para las claves de ingreso (letras y dígitos). */
    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";
    private static final String DIGITOS = "23456789";
    private static final String ALFABETO = LETRAS + DIGITOS;
    private static final int LONGITUD_CLAVE = 8;

    /** Clave válida: 8 caracteres alfanuméricos con al menos una letra y al menos un dígito. */
    private static final Pattern FORMATO_CLAVE =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9]{" + LONGITUD_CLAVE + "}$");

    private static final DateTimeFormatter FORMATO_NOMBRE_ARCHIVO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
    private static final DateTimeFormatter FORMATO_FECHA_EXCEL =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SecureRandom random = new SecureRandom();

    private final CandidatoDAO candidatoDAO;
    private final CuestionarioDAO cuestionarioDAO;
    private final PuestoDAO puestoDAO;
    private final FactorDAO factorDAO;
    private final EvaluacionDAO evaluacionDAO;
    private final GestorCuestionario gestorCuestionario;
    private final ContextoSeguridad contextoSeguridad;

    public GestorCandidatos(CandidatoDAO candidatoDAO,
                            CuestionarioDAO cuestionarioDAO,
                            PuestoDAO puestoDAO,
                            FactorDAO factorDAO,
                            EvaluacionDAO evaluacionDAO,
                            GestorCuestionario gestorCuestionario,
                            ContextoSeguridad contextoSeguridad) {
        this.candidatoDAO = candidatoDAO;
        this.cuestionarioDAO = cuestionarioDAO;
        this.puestoDAO = puestoDAO;
        this.factorDAO = factorDAO;
        this.evaluacionDAO = evaluacionDAO;
        this.gestorCuestionario = gestorCuestionario;
        this.contextoSeguridad = contextoSeguridad;
    }

    // ------------------------------------------------------------------
    // Pasos 2 a 5: búsqueda de candidatos
    // ------------------------------------------------------------------

    /**
     * Busca candidatos por apellido, nombre y/o número de candidato (CU 025 - mensajes 4 a 7).
     *
     * @param apellido     filtro de apellido (opcional)
     * @param nombre       filtro de nombre (opcional)
     * @param nroCandidato filtro de número de candidato (opcional)
     * @return candidatos que cumplen los criterios
     */
    @Transactional(readOnly = true)
    public List<CandidatoDTO> buscarCandidatos(String apellido, String nombre, Long nroCandidato) {
        return candidatoDAO.buscar(apellido, nombre, nroCandidato).stream()
                .map(CandidatoDTO::desde)
                .toList();
    }

    // ------------------------------------------------------------------
    // Paso 6: verificación de cuestionarios activos (flujo 6.A)
    // ------------------------------------------------------------------

    /**
     * Indica si el candidato NO posee cuestionarios activos o en proceso (CU 025 - mensaje 17).
     *
     * @param nroCandidato número de candidato
     * @return {@code true} si el candidato puede ser evaluado
     */
    @Transactional(readOnly = true)
    public boolean verificarSinCuestionarioActivo(Long nroCandidato) {
        return !cuestionarioDAO.tieneCuestionarioActivoOProceso(nroCandidato);
    }

    /**
     * Verifica que los candidatos seleccionados existan y que ninguno tenga cuestionarios activos
     * o en proceso (CU 025 - loop de los mensajes 17 a 21).
     *
     * @param nroCandidatos números de los candidatos de la grilla "Candidatos a evaluar"
     * @return candidatos validados, en el orden recibido y sin repetidos
     * @throws ReglaNegocioException si no se seleccionó ningún candidato (4.A) o si alguno
     *                               tiene cuestionarios activos o en proceso (6.A)
     * @throws RecursoNoEncontradoException si algún candidato no existe
     */
    @Transactional(readOnly = true)
    public List<CandidatoDTO> verificarCandidatosDisponibles(List<Long> nroCandidatos) {
        return obtenerCandidatosDisponibles(nroCandidatos).stream()
                .map(CandidatoDTO::desde)
                .toList();
    }

    /**
     * Bloquea las filas de los candidatos hasta que termine la transacción, para que dos
     * confirmaciones simultáneas no le generen dos cuestionarios activos al mismo candidato.
     * Se bloquean en orden ascendente para evitar bloqueos cruzados (deadlocks).
     */
    private void bloquearCandidatos(Set<Long> nroCandidatos) {
        nroCandidatos.stream()
                .filter(Objects::nonNull)
                .sorted()
                .forEach(candidatoDAO::bloquearPorNroCandidato);
    }

    private List<Candidato> obtenerCandidatosDisponibles(List<Long> nroCandidatos) {
        if (nroCandidatos == null || nroCandidatos.isEmpty()) {
            throw new ReglaNegocioException("Debe seleccionar uno o más candidatos");
        }

        Set<Long> sinRepetidos = new LinkedHashSet<>(nroCandidatos);
        List<Candidato> candidatos = new ArrayList<>();
        List<String> conCuestionarioActivo = new ArrayList<>();

        for (Long nro : sinRepetidos) {
            if (nro == null) {
                throw new ReglaNegocioException("El número de candidato es obligatorio");
            }
            Candidato candidato = candidatoDAO.findByNroCandidatoAndEliminadoFalse(nro)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No existe un candidato con el número " + nro));
            if (!verificarSinCuestionarioActivo(nro)) {
                conCuestionarioActivo.add(describir(candidato));
            }
            candidatos.add(candidato);
        }

        if (!conCuestionarioActivo.isEmpty()) {
            throw new ReglaNegocioException(
                    (conCuestionarioActivo.size() > 1
                            ? "Los siguientes candidatos poseen cuestionarios activos o en proceso y no pueden ser evaluados nuevamente: "
                            : "El siguiente candidato posee un cuestionario activo o en proceso y no puede ser evaluado nuevamente: ")
                            + String.join("; ", conCuestionarioActivo));
        }
        return candidatos;
    }

    // ------------------------------------------------------------------
    // Paso 9: validación de competencias evaluables (flujo 9.A)
    // ------------------------------------------------------------------

    /**
     * Valida que cada competencia del puesto posea al menos un factor con 2 o más preguntas
     * (CU 025 - mensajes 35 a 40).
     *
     * @param puestoCodigo código del puesto o función
     * @return resultado con la lista de competencias que no pueden evaluarse
     * @throws ReglaNegocioException si no se indicó el puesto (10.A)
     * @throws RecursoNoEncontradoException si el puesto no existe
     */
    @Transactional(readOnly = true)
    public ValidacionPuestoDTO validarCompetenciasEvaluables(String puestoCodigo) {
        Puesto puesto = obtenerPuesto(puestoCodigo);
        List<String> noEvaluables = competenciasNoEvaluables(puesto);
        return new ValidacionPuestoDTO(puesto.getCodigo(), noEvaluables.isEmpty(), noEvaluables);
    }

    private List<String> competenciasNoEvaluables(Puesto puesto) {
        List<String> noEvaluables = new ArrayList<>();
        for (CaracteristicaPuesto caracteristica : puesto.getCaracteristicas()) {
            List<Factor> factores = factorDAO
                    .findByCompetenciaIdOrderByNroOrdenAsc(caracteristica.getCompetencia().getId());
            boolean tieneFactorEvaluable = factores.stream()
                    .filter(f -> !Boolean.TRUE.equals(f.getEliminado()))
                    .anyMatch(Factor::tieneMinimoDosPreguntas);
            if (!tieneFactorEvaluable) {
                noEvaluables.add(caracteristica.getCompetencia().getNombre());
            }
        }
        return noEvaluables;
    }

    // ------------------------------------------------------------------
    // Paso 11: generación de claves para mostrar al consultor
    // ------------------------------------------------------------------

    /**
     * Prepara la evaluación: valida candidatos y puesto, y genera la clave de ingreso de cada
     * candidato para mostrarla en el paso 11. No persiste nada; los datos se guardan recién al
     * presionar "Finalizar" ({@link #evaluarCandidatos(ConfirmarEvaluacionRequestDTO)}).
     *
     * @param request candidatos seleccionados y puesto
     * @return candidatos a evaluar con su clave de ingreso
     */
    @Transactional(readOnly = true)
    public List<CandidatoClaveDTO> prepararEvaluacion(EvaluarCandidatosRequestDTO request) {
        List<Candidato> candidatos = obtenerCandidatosDisponibles(request.nroCandidatos());
        Puesto puesto = obtenerPuesto(request.puestoCodigo());
        validarPuestoEvaluable(puesto);

        Set<String> clavesUsadas = new HashSet<>();
        return candidatos.stream()
                .map(c -> CandidatoClaveDTO.desde(c, generarClaveUnica(clavesUsadas)))
                .toList();
    }

    // ------------------------------------------------------------------
    // Paso 12-13: Finalizar, se guarda la evaluación
    // ------------------------------------------------------------------

    /**
     * Evalúa a los candidatos en el puesto: genera la evaluación con un cuestionario ACTIVO por
     * candidato (CU 025 - mensajes 51 a 61).
     *
     * <p>Se vuelven a validar candidatos y puesto, ya que pudieron cambiar desde que se mostró la
     * pantalla. Se usan las claves mostradas al consultor en el paso 11; si alguna falta o no
     * cumple el formato, se genera una nueva.</p>
     *
     * @param request puesto y candidatos con las claves mostradas
     * @return evaluación creada con los candidatos y sus claves
     */
    @Transactional
    public EvaluacionCreadaDTO evaluarCandidatos(ConfirmarEvaluacionRequestDTO request) {
        if (request.candidatos() == null || request.candidatos().isEmpty()) {
            throw new ReglaNegocioException("Debe seleccionar uno o más candidatos");
        }
        Map<Long, String> clavePorCandidato = new LinkedHashMap<>();
        for (CandidatoClaveDTO dto : request.candidatos()) {
            clavePorCandidato.putIfAbsent(dto.nroCandidato(), dto.claveAcceso());
        }

        bloquearCandidatos(clavePorCandidato.keySet());
        List<Candidato> candidatos = obtenerCandidatosDisponibles(new ArrayList<>(clavePorCandidato.keySet()));
        Puesto puesto = obtenerPuesto(request.puestoCodigo());
        validarPuestoEvaluable(puesto);

        Set<String> clavesUsadas = new HashSet<>();
        Map<Candidato, String> candidatosConClave = new LinkedHashMap<>();
        for (Candidato candidato : candidatos) {
            String clave = clavePorCandidato.get(candidato.getNroCandidato());
            if (clave == null || !FORMATO_CLAVE.matcher(clave).matches() || !clavesUsadas.add(clave)) {
                clave = generarClaveUnica(clavesUsadas);
            }
            candidatosConClave.put(candidato, clave);
        }

        Evaluacion evaluacion = gestorCuestionario.generarCuestionariosParaEvaluacion(
                candidatosConClave, puesto, contextoSeguridad.getUsuarioActualId());

        List<CandidatoClaveDTO> resultado = candidatosConClave.entrySet().stream()
                .map(e -> CandidatoClaveDTO.desde(e.getKey(), e.getValue()))
                .toList();

        log.info("CU025: {} candidato(s) evaluados para el puesto {} (evaluación {})",
                resultado.size(), puesto.getCodigo(), evaluacion.getId());

        return new EvaluacionCreadaDTO(evaluacion.getId(), puesto.getCodigo(), puesto.getNombre(),
                puesto.getEmpresa(), evaluacion.getFechaCreacion(), resultado);
    }

    /**
     * Genera una clave de ingreso aleatoria de 8 caracteres con letras y dígitos
     * (CU 025 - mensaje 56, requerimiento especial).
     *
     * <p>Se excluyen caracteres que se confunden fácilmente al leerlos (0/O, 1/l/I).</p>
     *
     * @return clave de ingreso
     */
    public String generarClaveAcceso() {
        String clave;
        do {
            StringBuilder sb = new StringBuilder(LONGITUD_CLAVE);
            for (int i = 0; i < LONGITUD_CLAVE; i++) {
                sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
            }
            clave = sb.toString();
        } while (!FORMATO_CLAVE.matcher(clave).matches());
        return clave;
    }

    private String generarClaveUnica(Set<String> clavesUsadas) {
        String clave;
        do {
            clave = generarClaveAcceso();
        } while (!clavesUsadas.add(clave));
        return clave;
    }

    // ------------------------------------------------------------------
    // Pasos 14 a 17: exportación a Excel
    // ------------------------------------------------------------------

    /**
     * Genera el archivo Excel (.xls) con los candidatos de la evaluación y sus claves
     * (CU 025 - mensajes 70 y 71).
     *
     * <p>El nombre del archivo tiene el formato {@code aaaa-mm-dd-hh-mm-NombreFunción.xls},
     * tomando la fecha y hora en que se generó la evaluación.</p>
     *
     * @param evaluacionId identificador de la evaluación
     * @return archivo generado
     * @throws RecursoNoEncontradoException si la evaluación no existe
     */
    @Transactional(readOnly = true)
    public ArchivoExcelDTO exportarResultados(Long evaluacionId) {
        Evaluacion evaluacion = evaluacionDAO.obtenerPorId(evaluacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evaluación", evaluacionId));
        Puesto puesto = evaluacion.getPuesto();

        List<Cuestionario> cuestionarios = evaluacion.getCuestionarios().stream()
                .sorted(Comparator.comparing(Cuestionario::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        try (Workbook libro = new HSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Candidatos");

            Font negrita = libro.createFont();
            negrita.setBold(true);
            CellStyle estiloTitulo = libro.createCellStyle();
            estiloTitulo.setFont(negrita);

            int fila = 0;
            fila = escribirFila(hoja, fila, estiloTitulo, "Evaluación de candidatos");
            fila = escribirFila(hoja, fila, null, "Puesto / Función:", puesto.getNombre());
            fila = escribirFila(hoja, fila, null, "Empresa:", puesto.getEmpresa());
            fila = escribirFila(hoja, fila, null, "Fecha:", evaluacion.getFechaCreacion().format(FORMATO_FECHA_EXCEL));
            fila++;
            fila = escribirFila(hoja, fila, estiloTitulo,
                    "Apellido", "Nombre", "Tipo documento", "Nro. documento", "Clave de ingreso");
            for (Cuestionario q : cuestionarios) {
                Candidato c = q.getCandidato();
                fila = escribirFila(hoja, fila, null, c.getApellido(), c.getNombre(),
                        c.getTipoDocumento().name(), c.getNroDocumento(), q.getClaveAcceso());
            }
            for (int col = 0; col < 5; col++) {
                hoja.autoSizeColumn(col);
            }

            libro.write(salida);
            return new ArchivoExcelDTO(nombreArchivo(evaluacion.getFechaCreacion(), puesto.getNombre()),
                    salida.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el archivo Excel", e);
        }
    }

    private static int escribirFila(Sheet hoja, int numeroFila, CellStyle estilo, String... valores) {
        Row row = hoja.createRow(numeroFila);
        for (int i = 0; i < valores.length; i++) {
            var celda = row.createCell(i);
            celda.setCellValue(valores[i]);
            if (estilo != null) {
                celda.setCellStyle(estilo);
            }
        }
        return numeroFila + 1;
    }

    /**
     * Arma el nombre del archivo Excel: {@code aaaa-mm-dd-hh-mm-NombreFunción.xls}.
     * Se quitan los caracteres no permitidos en nombres de archivo.
     */
    static String nombreArchivo(LocalDateTime fecha, String nombreFuncion) {
        String nombreLimpio = nombreFuncion == null ? "" : nombreFuncion.replaceAll("[\\\\/:*?\"<>|]", "").trim();
        return fecha.format(FORMATO_NOMBRE_ARCHIVO) + "-" + nombreLimpio + ".xls";
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private Puesto obtenerPuesto(String puestoCodigo) {
        if (puestoCodigo == null || puestoCodigo.isBlank()) {
            throw new ReglaNegocioException("Debe seleccionar una función o puesto");
        }
        String codigo = puestoCodigo.trim().toUpperCase(Locale.ROOT);
        return puestoDAO.findByCodigoAndEliminadoFalse(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un puesto con el código '" + codigo + "'"));
    }

    private void validarPuestoEvaluable(Puesto puesto) {
        List<String> noEvaluables = competenciasNoEvaluables(puesto);
        if (!noEvaluables.isEmpty()) {
            throw new ReglaNegocioException(
                    "Las siguientes competencias no pueden ser evaluadas: " + String.join(", ", noEvaluables));
        }
    }

    private static String describir(Candidato c) {
        return c.getApellido() + ", " + c.getNombre() + " (Nro. " + c.getNroCandidato() + ")";
    }
}
