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
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
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
            XWPFParagraph titulo = documento.createParagraph();
            titulo.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun tituloRun = titulo.createRun();
            tituloRun.setText("ORDEN DE MÉRITO");
            tituloRun.setBold(true);
            tituloRun.setFontSize(18);
            tituloRun.setFontFamily("Arial");

            agregarParrafo(documento, "Puesto: " + texto(informe.codigoPuesto()) + " - " + texto(informe.nombrePuesto()), true);
            agregarParrafo(documento, "Empresa: " + texto(informe.empresa()), false);
            if (informe.descripcionPuesto() != null && !informe.descripcionPuesto().isBlank()) {
                agregarParrafo(documento, "Descripción: " + informe.descripcionPuesto(), false);
            }
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
