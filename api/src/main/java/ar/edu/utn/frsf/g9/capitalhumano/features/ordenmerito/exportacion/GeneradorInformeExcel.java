package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.exportacion;

import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.CandidatoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.EvaluacionOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.InformeOrdenMeritoDTO;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class GeneradorInformeExcel implements GeneradorInformeOrdenMerito {
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String MENSAJE_SIN_DATOS = "No hay información disponible para esta sección.";
    private static final int FILA_ENCABEZADO = 6;

    @Override
    public FormatoInformeOrdenMerito formato() {
        return FormatoInformeOrdenMerito.EXCEL;
    }

    @Override
    public byte[] generar(InformeOrdenMeritoDTO informe) throws IOException {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            CellStyle titulo = crearEstiloTitulo(libro);
            CellStyle encabezado = crearEstiloEncabezado(libro);
            CellStyle celda = crearEstiloCelda(libro);

            crearHojaCaratula(libro, informe, crearEstiloCaratula(libro), crearEstiloDatoCaratula(libro), celda);
            crearHojaEvaluaciones(libro, informe, titulo, encabezado, celda);
            crearHojaCandidatos(libro, "Aptos", "Candidatos aptos", filtrar(informe.candidatos(), "APTO"),
                    true, informe, titulo, encabezado, celda);
            crearHojaCandidatos(libro, "No alcanzan mínimos", "No alcanzan los mínimos",
                    filtrar(informe.candidatos(), "NO_ALCANZA_MINIMOS"), false, informe, titulo, encabezado, celda);
            crearHojaCandidatos(libro, "Puntaje pendiente", "Puntaje pendiente",
                    filtrar(informe.candidatos(), "PENDIENTE_PUNTAJE"), false, informe, titulo, encabezado, celda);
            crearHojaCandidatos(libro, "Cuestionarios incompletos", "Cuestionarios incompletos",
                    filtrar(informe.candidatos(), "CUESTIONARIO_INCOMPLETO"), false, informe, titulo, encabezado, celda);

            libro.write(salida);
            return salida.toByteArray();
        }
    }

    private void crearHojaEvaluaciones(Workbook libro, InformeOrdenMeritoDTO informe, CellStyle titulo,
                                       CellStyle encabezado, CellStyle celda) {
        Sheet hoja = libro.createSheet("Evaluaciones");
        agregarMetadatos(hoja, informe, titulo, celda);
        Row seccion = hoja.createRow(5);
        agregarCelda(seccion, 0, "Evaluaciones incluidas", encabezado);

        String[] columnas = {"N°", "Cuestionario", "Candidato", "Fecha", "Estado"};
        Row filaEncabezado = hoja.createRow(FILA_ENCABEZADO);
        agregarFila(filaEncabezado, columnas, encabezado);
        int indice = 7;
        int numero = 1;
        for (EvaluacionOrdenMeritoDTO evaluacion : informe.evaluaciones()) {
            Row fila = hoja.createRow(indice++);
            agregarFila(fila, new String[]{String.valueOf(numero++), texto(evaluacion.id()), texto(evaluacion.proyecto()),
                    texto(evaluacion.fecha()), texto(evaluacion.estado())}, celda);
        }
        if (informe.evaluaciones().isEmpty()) {
            agregarCelda(hoja.createRow(indice), 0, MENSAJE_SIN_DATOS, celda);
        }
        configurarImpresion(hoja, columnas.length, indice);
        ajustarColumnas(hoja, columnas.length);
    }

    private void crearHojaCaratula(Workbook libro, InformeOrdenMeritoDTO informe, CellStyle titulo,
                                   CellStyle dato, CellStyle celda) {
        Sheet hoja = libro.createSheet("Carátula");
        agregarCelda(hoja.createRow(0), 0, "INFORME DE SELECCIÓN · ORDEN DE MÉRITO", titulo);
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        Row bajada = hoja.createRow(1);
        agregarCelda(bajada, 0, "Resultado de evaluación de candidatos", dato);
        hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

        agregarDatoCaratula(hoja, 3, "Puesto", texto(informe.codigoPuesto()) + "  |  " + texto(informe.nombrePuesto()), dato);
        agregarDatoCaratula(hoja, 4, "Empresa", texto(informe.empresa()), dato);
        agregarDatoCaratula(hoja, 5, "Descripción", texto(informe.descripcionPuesto()), dato);
        agregarDatoCaratula(hoja, 6, "Fecha de emisión", FECHA_HORA.format(informe.fechaGeneracion()), dato);

        String[] etiquetas = {"Evaluaciones", "Candidatos evaluados", "Candidatos aptos", "No alcanzan mínimos", "Puntaje pendiente"};
        String[] valores = {String.valueOf(informe.evaluaciones().size()), String.valueOf(informe.candidatos().size()),
                String.valueOf(contar(informe, "APTO")), String.valueOf(contar(informe, "NO_ALCANZA_MINIMOS")),
                String.valueOf(contar(informe, "PENDIENTE_PUNTAJE"))};
        Row filaEtiquetas = hoja.createRow(8);
        Row filaValores = hoja.createRow(9);
        for (int i = 0; i < etiquetas.length; i++) {
            agregarCelda(filaEtiquetas, i, etiquetas[i], titulo);
            agregarCelda(filaValores, i, valores[i], dato);
        }

        Row seccion = hoja.createRow(11);
        agregarCelda(seccion, 0, "Evaluaciones incluidas", titulo);
        hoja.addMergedRegion(new CellRangeAddress(11, 11, 0, 4));
        String[] columnas = {"N°", "Cuestionario", "Candidato / proyecto", "Fecha", "Estado"};
        agregarFila(hoja.createRow(12), columnas, titulo);
        int indice = 13;
        int numero = 1;
        for (EvaluacionOrdenMeritoDTO evaluacion : informe.evaluaciones()) {
            Row fila = hoja.createRow(indice++);
            agregarFila(fila, new String[]{String.valueOf(numero++), texto(evaluacion.id()), texto(evaluacion.proyecto()),
                    texto(evaluacion.fecha()), texto(evaluacion.estado())}, celda);
        }
        if (informe.evaluaciones().isEmpty()) {
            agregarCelda(hoja.createRow(indice), 0, MENSAJE_SIN_DATOS, celda);
        }

        hoja.setColumnWidth(0, 3_000);
        hoja.setColumnWidth(1, 4_000);
        hoja.setColumnWidth(2, 10_000);
        hoja.setColumnWidth(3, 5_000);
        hoja.setColumnWidth(4, 5_000);
        hoja.getRow(0).setHeightInPoints(30);
        hoja.getRow(5).setHeightInPoints(30);
        hoja.getPrintSetup().setLandscape(true);
        hoja.setFitToPage(true);
        hoja.getPrintSetup().setFitWidth((short) 1);
        hoja.getPrintSetup().setFitHeight((short) 1);
        libro.setPrintArea(libro.getSheetIndex(hoja), 0, 4, 0, Math.max(12, indice - 1));
    }

    private void agregarDatoCaratula(Sheet hoja, int fila, String etiqueta, String valor, CellStyle estilo) {
        Row registro = hoja.createRow(fila);
        agregarCelda(registro, 0, etiqueta, estilo);
        agregarCelda(registro, 1, valor, estilo);
        hoja.addMergedRegion(new CellRangeAddress(fila, fila, 1, 4));
    }

    private CellStyle crearEstiloCaratula(Workbook libro) {
        CellStyle estilo = crearEstiloCelda(libro);
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 16);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }

    private CellStyle crearEstiloDatoCaratula(Workbook libro) {
        CellStyle estilo = crearEstiloCelda(libro);
        Font fuente = libro.createFont();
        fuente.setColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }

    private long contar(InformeOrdenMeritoDTO informe, String estado) {
        return informe.candidatos().stream().filter(candidato -> estado.equals(candidato.estadoMerito())).count();
    }

    private void crearHojaCandidatos(Workbook libro, String nombreHoja, String tituloSeccion,
                                     List<CandidatoOrdenMeritoDTO> candidatos, boolean incluirOrden,
                                     InformeOrdenMeritoDTO informe, CellStyle titulo, CellStyle encabezado,
                                     CellStyle celda) {
        Sheet hoja = libro.createSheet(nombreHoja);
        agregarMetadatos(hoja, informe, titulo, celda);
        Row seccion = hoja.createRow(5);
        agregarCelda(seccion, 0, tituloSeccion, encabezado);

        String[] columnas = incluirOrden
                ? new String[]{"Orden", "Cuestionario", "Documento", "Candidato", "Estado del cuestionario", "Puntaje", "Inicio", "Finalización"}
                : new String[]{"Cuestionario", "Documento", "Candidato", "Estado del cuestionario", "Puntaje", "Estado de mérito", "Inicio", "Finalización"};
        Row filaEncabezado = hoja.createRow(FILA_ENCABEZADO);
        agregarFila(filaEncabezado, columnas, encabezado);

        if (candidatos.isEmpty()) {
            agregarCelda(hoja.createRow(FILA_ENCABEZADO + 1), 0, MENSAJE_SIN_DATOS, celda);
            configurarImpresion(hoja, columnas.length, FILA_ENCABEZADO + 1);
            ajustarColumnas(hoja, columnas.length);
            return;
        }

        int indice = FILA_ENCABEZADO + 1;
        int orden = 1;
        for (CandidatoOrdenMeritoDTO candidato : candidatos) {
            String nombre = texto(candidato.apellido()) + ", " + texto(candidato.nombre());
            String puntaje = candidato.puntajeObtenido() == null
                    ? "Pendiente" : String.format(Locale.ROOT, "%.2f", candidato.puntajeObtenido());
            String inicio = candidato.fechaInicio() == null ? "Sin iniciar" : FECHA_HORA.format(candidato.fechaInicio());
            String fin = candidato.fechaFinalizacion() == null ? "Sin completar" : FECHA_HORA.format(candidato.fechaFinalizacion());
            String[] valores = incluirOrden
                    ? new String[]{String.valueOf(orden++), texto(candidato.cuestionarioId()), documento(candidato), nombre,
                    estadoCuestionario(candidato.estadoCuestionario()), puntaje, inicio, fin}
                    : new String[]{texto(candidato.cuestionarioId()), documento(candidato), nombre,
                    estadoCuestionario(candidato.estadoCuestionario()), puntaje, estadoMerito(candidato.estadoMerito()), inicio, fin};
            agregarFila(hoja.createRow(indice++), valores, celda);
        }
        configurarImpresion(hoja, columnas.length, indice - 1);
        ajustarColumnas(hoja, columnas.length);
    }

    private void agregarMetadatos(Sheet hoja, InformeOrdenMeritoDTO informe, CellStyle titulo, CellStyle celda) {
        agregarCelda(hoja.createRow(0), 0, "ORDEN DE MÉRITO", titulo);
        agregarCelda(hoja.createRow(1), 0,
                "Puesto: " + texto(informe.codigoPuesto()) + " - " + texto(informe.nombrePuesto()), celda);
        agregarCelda(hoja.createRow(2), 0, "Empresa: " + texto(informe.empresa()), celda);
        agregarCelda(hoja.createRow(3), 0,
                "Fecha de generación: " + FECHA_HORA.format(informe.fechaGeneracion()), celda);
    }

    private void configurarImpresion(Sheet hoja, int cantidadColumnas, int ultimaFila) {
        hoja.createFreezePane(0, FILA_ENCABEZADO + 1);
        hoja.setRepeatingRows(new CellRangeAddress(FILA_ENCABEZADO, FILA_ENCABEZADO, -1, -1));
        hoja.setAutoFilter(new CellRangeAddress(FILA_ENCABEZADO, Math.max(FILA_ENCABEZADO, ultimaFila),
                0, cantidadColumnas - 1));
        hoja.getPrintSetup().setLandscape(true);
        hoja.setFitToPage(true);
        hoja.getPrintSetup().setFitWidth((short) 1);
        hoja.getPrintSetup().setFitHeight((short) 0);
    }

    private void ajustarColumnas(Sheet hoja, int cantidad) {
        for (int indice = 0; indice < cantidad; indice++) {
            hoja.autoSizeColumn(indice);
            hoja.setColumnWidth(indice, Math.min(hoja.getColumnWidth(indice) + 512, 12_000));
        }
        hoja.setColumnWidth(0, Math.max(hoja.getColumnWidth(0), 2_800));
    }

    private CellStyle crearEstiloTitulo(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 16);
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }

    private CellStyle crearEstiloEncabezado(Workbook libro) {
        CellStyle estilo = crearEstiloCelda(libro);
        Font fuente = libro.createFont();
        fuente.setBold(true);
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }

    private CellStyle crearEstiloCelda(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        estilo.setWrapText(true);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        estilo.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        estilo.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        estilo.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        estilo.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        return estilo;
    }

    private void agregarFila(Row fila, String[] valores, CellStyle estilo) {
        for (int indice = 0; indice < valores.length; indice++) {
            agregarCelda(fila, indice, valores[indice], estilo);
        }
    }

    private void agregarCelda(Row fila, int indice, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(indice);
        celda.setCellValue(texto(valor));
        celda.setCellStyle(estilo);
    }

    private List<CandidatoOrdenMeritoDTO> filtrar(List<CandidatoOrdenMeritoDTO> candidatos, String estado) {
        return candidatos.stream().filter(candidato -> estado.equals(candidato.estadoMerito())).toList();
    }

    private String documento(CandidatoOrdenMeritoDTO candidato) {
        return texto(candidato.tipoDocumento()) + " " + texto(candidato.nroDocumento());
    }

    private String estadoCuestionario(String estado) {
        return texto(estado).replace('_', ' ');
    }

    private String estadoMerito(String estado) {
        return switch (estado) {
            case "APTO" -> "Apto";
            case "NO_ALCANZA_MINIMOS" -> "No alcanza los mínimos";
            case "PENDIENTE_PUNTAJE" -> "Puntaje pendiente";
            default -> "Cuestionario incompleto";
        };
    }

    private String texto(Object valor) {
        return valor == null ? "-" : valor.toString();
    }
}
