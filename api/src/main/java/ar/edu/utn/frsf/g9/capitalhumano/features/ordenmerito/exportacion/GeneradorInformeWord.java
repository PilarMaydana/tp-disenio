package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.exportacion;

import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.CandidatoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.EvaluacionOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.InformeOrdenMeritoDTO;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class GeneradorInformeWord implements GeneradorInformeOrdenMerito {
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String MENSAJE_SIN_DATOS = "No hay información disponible para esta sección.";

    @Override
    public FormatoInformeOrdenMerito formato() { return FormatoInformeOrdenMerito.WORD; }

    @Override
    public byte[] generar(InformeOrdenMeritoDTO informe) throws IOException {
        try (XWPFDocument documento = new XWPFDocument(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            configurarPaginaHorizontal(documento);
            configurarNumeracion(documento);
            agregarPortada(documento, informe);
            documento.createParagraph().setPageBreak(true);
            agregarParrafo(documento, "Fecha de generación: " + FECHA_HORA.format(informe.fechaGeneracion()), false);

            agregarTablaEvaluaciones(documento, informe.evaluaciones());
            agregarTablaResultados(documento, "Candidatos aptos", filtrar(informe.candidatos(), "APTO"), true);
            agregarTablaResultados(documento, "No alcanzan los mínimos", filtrar(informe.candidatos(), "NO_ALCANZA_MINIMOS"), false);
            agregarTablaResultados(documento, "Puntaje pendiente", filtrar(informe.candidatos(), "PENDIENTE_PUNTAJE"), false);
            agregarTablaResultados(documento, "Cuestionarios incompletos", filtrar(informe.candidatos(), "CUESTIONARIO_INCOMPLETO"), false);

            documento.write(salida);
            return salida.toByteArray();
        }
    }

    private void configurarNumeracion(XWPFDocument documento) {
        XWPFHeaderFooterPolicy policy = documento.createHeaderFooterPolicy();
        XWPFFooter footer = policy.createFooter(STHdrFtr.DEFAULT);
        XWPFParagraph parrafo = footer.createParagraph();
        parrafo.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun inicio = parrafo.createRun();
        inicio.setText("Página ");
        inicio.setFontFamily("Arial");
        inicio.setFontSize(8);
        var campoPagina = parrafo.getCTP().addNewFldSimple();
        campoPagina.setInstr("PAGE");
        XWPFRun separador = parrafo.createRun();
        separador.setText(" de ");
        separador.setFontSize(8);
        var campoTotal = parrafo.getCTP().addNewFldSimple();
        campoTotal.setInstr("NUMPAGES");
    }

    private void agregarPortada(XWPFDocument documento, InformeOrdenMeritoDTO informe) {
        XWPFParagraph encabezado = documento.createParagraph();
        encabezado.setAlignment(ParagraphAlignment.CENTER);
        encabezado.setSpacingBefore(1200);
        XWPFRun titulo = encabezado.createRun();
        titulo.setText("INFORME DE SELECCIÓN");
        titulo.setFontFamily("Arial");
        titulo.setFontSize(11);
        titulo.setBold(true);
        titulo.setColor("3F2581");
        titulo.addBreak();
        titulo.setText("ORDEN DE MÉRITO");
        titulo.setFontSize(26);

        XWPFParagraph bajada = documento.createParagraph();
        bajada.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun bajadaRun = bajada.createRun();
        bajadaRun.setText("Resultado de evaluación de candidatos");
        bajadaRun.setFontFamily("Arial");
        bajadaRun.setFontSize(12);
        bajadaRun.setColor("757575");

        agregarParrafoPortada(documento, "PUESTO", true, "3F2581", 10);
        agregarParrafoPortada(documento, texto(informe.codigoPuesto()) + "  |  " + texto(informe.nombrePuesto()), true, "222222", 16);
        agregarParrafoPortada(documento, "Empresa: " + texto(informe.empresa()), false, "333333", 11);
        if (informe.descripcionPuesto() != null && !informe.descripcionPuesto().isBlank()) {
            agregarParrafoPortada(documento, "Descripción: " + informe.descripcionPuesto(), false, "555555", 10);
        }
        agregarParrafoPortada(documento, "Fecha de emisión: " + FECHA_HORA.format(informe.fechaGeneracion()), false, "555555", 10);

        XWPFTable resumen = documento.createTable(2, 4);
        resumen.setWidth("100%");
        String[] etiquetas = {"EVALUACIONES", "CANDIDATOS EVALUADOS", "CANDIDATOS APTOS", "NO ALCANZAN MÍNIMOS"};
        String[] valores = {String.valueOf(informe.evaluaciones().size()), String.valueOf(informe.candidatos().size()),
                String.valueOf(contar(informe, "APTO")), String.valueOf(contar(informe, "NO_ALCANZA_MINIMOS"))};
        for (int i = 0; i < etiquetas.length; i++) {
            XWPFTableCell etiqueta = resumen.getRow(0).getCell(i);
            etiqueta.setColor("3F2581");
            etiqueta.setText("");
            XWPFRun etiquetaRun = etiqueta.getParagraphs().getFirst().createRun();
            etiquetaRun.setText(etiquetas[i]);
            etiquetaRun.setBold(true);
            etiquetaRun.setColor("FFFFFF");
            etiquetaRun.setFontFamily("Arial");
            etiquetaRun.setFontSize(8);

            XWPFTableCell valor = resumen.getRow(1).getCell(i);
            valor.setColor("F4F0FF");
            valor.setText("");
            XWPFRun valorRun = valor.getParagraphs().getFirst().createRun();
            valorRun.setText(valores[i]);
            valorRun.setBold(true);
            valorRun.setColor("3F2581");
            valorRun.setFontFamily("Arial");
            valorRun.setFontSize(14);
        }

        agregarParrafoPortada(documento, "Estado de los candidatos", true, "3F2581", 10);
        agregarParrafoPortada(documento, "Aptos: " + contar(informe, "APTO")
                + "  |  No alcanzan los mínimos: " + contar(informe, "NO_ALCANZA_MINIMOS")
                + "  |  Puntaje pendiente: " + contar(informe, "PENDIENTE_PUNTAJE")
                + "  |  Cuestionarios incompletos: " + contar(informe, "CUESTIONARIO_INCOMPLETO"), false, "333333", 9);
        agregarParrafoPortada(documento, "Evaluaciones incluidas", true, "3F2581", 10);
        int cantidadVisible = Math.min(informe.evaluaciones().size(), 5);
        for (int i = 0; i < cantidadVisible; i++) {
            EvaluacionOrdenMeritoDTO evaluacion = informe.evaluaciones().get(i);
            agregarParrafoPortada(documento, "•  " + texto(evaluacion.proyecto()) + "  |  "
                    + texto(evaluacion.fecha()) + "  |  " + texto(evaluacion.estado()), false, "555555", 9);
        }
        if (informe.evaluaciones().size() > cantidadVisible) {
            agregarParrafoPortada(documento, "Y " + (informe.evaluaciones().size() - cantidadVisible)
                    + " evaluación(es) más, detalladas en las páginas siguientes.", false, "555555", 9);
        }
    }

    private void agregarParrafoPortada(XWPFDocument documento, String texto, boolean destacado, String color, int tamanio) {
        XWPFParagraph parrafo = documento.createParagraph();
        parrafo.setSpacingAfter(80);
        XWPFRun run = parrafo.createRun();
        run.setText(texto);
        run.setFontFamily("Arial");
        run.setFontSize(tamanio);
        run.setBold(destacado);
        run.setColor(color);
    }

    private long contar(InformeOrdenMeritoDTO informe, String estado) {
        return informe.candidatos().stream().filter(candidato -> estado.equals(candidato.estadoMerito())).count();
    }

    private void configurarPaginaHorizontal(XWPFDocument documento) {
        CTSectPr seccion = documento.getDocument().getBody().isSetSectPr()
                ? documento.getDocument().getBody().getSectPr()
                : documento.getDocument().getBody().addNewSectPr();
        CTPageSz pagina = seccion.isSetPgSz() ? seccion.getPgSz() : seccion.addNewPgSz();
        pagina.setOrient(STPageOrientation.LANDSCAPE);
        pagina.setW(BigInteger.valueOf(15840));
        pagina.setH(BigInteger.valueOf(12240));
        CTPageMar margenes = seccion.isSetPgMar() ? seccion.getPgMar() : seccion.addNewPgMar();
        margenes.setTop(BigInteger.valueOf(720));
        margenes.setBottom(BigInteger.valueOf(720));
        margenes.setLeft(BigInteger.valueOf(720));
        margenes.setRight(BigInteger.valueOf(720));
    }

    private void agregarParrafo(XWPFDocument documento, String texto, boolean destacado) {
        XWPFParagraph parrafo = documento.createParagraph();
        XWPFRun run = parrafo.createRun();
        run.setText(texto);
        run.setFontFamily("Arial");
        run.setFontSize(10);
        run.setBold(destacado);
    }

    private void agregarTablaEvaluaciones(XWPFDocument documento, List<EvaluacionOrdenMeritoDTO> evaluaciones) {
        agregarParrafo(documento, "Evaluaciones incluidas", true);
        if (evaluaciones.isEmpty()) {
            agregarParrafo(documento, MENSAJE_SIN_DATOS, false);
            return;
        }
        XWPFTable tabla = nuevaTabla(documento, new String[]{"N°", "Cuestionario", "Candidato", "Fecha", "Estado"});
        int numero = 1;
        for (EvaluacionOrdenMeritoDTO evaluacion : evaluaciones) {
            XWPFTableRow fila = tabla.createRow();
            String[] valores = {String.valueOf(numero++), String.valueOf(evaluacion.id()), evaluacion.proyecto(),
                    evaluacion.fecha(), evaluacion.estado()};
            completarFila(fila, valores, false);
        }
    }

    private void agregarTablaResultados(XWPFDocument documento, String titulo, List<CandidatoOrdenMeritoDTO> candidatos,
                                        boolean incluirOrden) {
        agregarParrafo(documento, titulo, true);
        if (candidatos.isEmpty()) {
            agregarParrafo(documento, MENSAJE_SIN_DATOS, false);
            return;
        }
        String[] encabezados = incluirOrden
                ? new String[]{"Orden", "Cuestionario", "Documento", "Candidato", "Estado del cuestionario", "Puntaje", "Inicio", "Finalización"}
                : new String[]{"Cuestionario", "Documento", "Candidato", "Estado del cuestionario", "Puntaje", "Estado de mérito", "Inicio", "Finalización"};
        XWPFTable tabla = nuevaTabla(documento, encabezados);
        int orden = 1;
        for (CandidatoOrdenMeritoDTO candidato : candidatos) {
            String nombre = candidato.apellido() + ", " + candidato.nombre();
            String puntaje = candidato.puntajeObtenido() == null ? "Pendiente" : String.format("%.2f", candidato.puntajeObtenido());
            String inicio = candidato.fechaInicio() == null ? "Sin iniciar" : FECHA_HORA.format(candidato.fechaInicio());
            String fin = candidato.fechaFinalizacion() == null ? "Sin completar" : FECHA_HORA.format(candidato.fechaFinalizacion());
            String[] valores = incluirOrden
                    ? new String[]{String.valueOf(orden++), String.valueOf(candidato.cuestionarioId()), documento(candidato), nombre,
                    estadoCuestionario(candidato.estadoCuestionario()), puntaje, inicio, fin}
                    : new String[]{String.valueOf(candidato.cuestionarioId()), documento(candidato), nombre,
                    estadoCuestionario(candidato.estadoCuestionario()), puntaje, estadoMerito(candidato.estadoMerito()), inicio, fin};
            completarFila(tabla.createRow(), valores, false);
        }
    }

    private XWPFTable nuevaTabla(XWPFDocument documento, String[] encabezados) {
        XWPFTable tabla = documento.createTable(1, encabezados.length);
        tabla.setWidth("100%");
        tabla.setInsideHBorder(XWPFBorderType.SINGLE, 6, 0, "777777");
        tabla.setInsideVBorder(XWPFBorderType.SINGLE, 6, 0, "777777");
        tabla.setTopBorder(XWPFBorderType.SINGLE, 6, 0, "777777");
        tabla.setBottomBorder(XWPFBorderType.SINGLE, 6, 0, "777777");
        completarFila(tabla.getRow(0), encabezados, true);
        CTTrPr propiedadesFila = tabla.getRow(0).getCtRow().isSetTrPr()
                ? tabla.getRow(0).getCtRow().getTrPr() : tabla.getRow(0).getCtRow().addNewTrPr();
        CTOnOff encabezadoRepetible = propiedadesFila.addNewTblHeader();
        encabezadoRepetible.setVal(Boolean.TRUE);
        return tabla;
    }

    private void completarFila(XWPFTableRow fila, String[] valores, boolean encabezado) {
        for (int i = 0; i < valores.length; i++) {
            XWPFTableCell celda = i < fila.getTableCells().size() ? fila.getCell(i) : fila.addNewTableCell();
            celda.setColor(encabezado ? "D9D9D9" : "F2F2F2");
            celda.setText("");
            XWPFParagraph parrafo = celda.getParagraphs().getFirst();
            parrafo.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = parrafo.createRun();
            run.setText(texto(valores[i]));
            run.setFontFamily("Arial");
            run.setFontSize(8);
            run.setBold(encabezado);
        }
    }

    private List<CandidatoOrdenMeritoDTO> filtrar(List<CandidatoOrdenMeritoDTO> candidatos, String estado) {
        return candidatos.stream().filter(c -> estado.equals(c.estadoMerito())).toList();
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

    private String texto(String valor) { return valor == null ? "-" : valor; }
}
