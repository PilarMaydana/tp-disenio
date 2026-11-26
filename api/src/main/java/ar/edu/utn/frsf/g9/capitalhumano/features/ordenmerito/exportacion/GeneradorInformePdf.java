package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.exportacion;

import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.CandidatoOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.EvaluacionOrdenMeritoDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.InformeOrdenMeritoDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class GeneradorInformePdf implements GeneradorInformeOrdenMerito {
    private static final float MARGEN = 32;
    private static final float ANCHO_PAGINA = PDRectangle.A4.getHeight();
    private static final float ALTO_PAGINA = PDRectangle.A4.getWidth();
    private static final float ANCHO_TABLA = ANCHO_PAGINA - MARGEN * 2;
    private static final float[] PESOS_COLUMNAS = {0.045f, 0.055f, 0.09f, 0.12f, 0.10f, 0.06f, 0.12f, 0.10f, 0.10f, 0.21f};
    private static final String MENSAJE_SIN_DATOS = "No hay información disponible para esta sección.";
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final PDType1Font NORMAL = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font NEGRITA = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    @Override
    public FormatoInformeOrdenMerito formato() { return FormatoInformeOrdenMerito.PDF; }

    @Override
    public byte[] generar(InformeOrdenMeritoDTO informe) throws IOException {
        try (PDDocument documento = new PDDocument(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PdfBuilder pdf = new PdfBuilder(documento, informe);
            pdf.nuevaPagina(true);
            pdf.agregarTextoCentrado("ORDEN DE MÉRITO", 17, true);
            pdf.agregarTextoCentrado("Puesto: " + texto(informe.codigoPuesto()) + " - " + texto(informe.nombrePuesto()), 12, true);
            pdf.agregarTextoCentrado("Empresa: " + texto(informe.empresa()), 11, false);

            pdf.nuevaPagina(false);
            pdf.agregarTexto("Generado: " + FECHA_HORA.format(informe.fechaGeneracion()), 9, false);
            agregarTablaEvaluaciones(pdf, informe.evaluaciones());
            agregarTablaCandidatos(pdf, "Candidatos aptos", filtrar(informe.candidatos(), "APTO"));
            agregarTablaCandidatos(pdf, "No alcanzan los mínimos", filtrar(informe.candidatos(), "NO_ALCANZA_MINIMOS"));
            agregarTablaCandidatos(pdf, "Puntaje pendiente", filtrar(informe.candidatos(), "PENDIENTE_PUNTAJE"));
            agregarTablaCandidatos(pdf, "Cuestionarios incompletos", filtrar(informe.candidatos(), "CUESTIONARIO_INCOMPLETO"));

            pdf.cerrarPagina();
            agregarNumeracion(documento);
            documento.save(salida);
            return salida.toByteArray();
        }
    }

    private void agregarNumeracion(PDDocument documento) throws IOException {
        int total = documento.getNumberOfPages();
        for (int indice = 0; indice < total; indice++) {
            PDPage pagina = documento.getPage(indice);
            String etiqueta = "Página " + (indice + 1) + " de " + total;
            float ancho = NORMAL.getStringWidth(etiqueta) / 1000 * 8;
            try (PDPageContentStream pie = new PDPageContentStream(documento, pagina,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                pie.beginText();
                pie.setFont(NORMAL, 8);
                pie.newLineAtOffset(ANCHO_PAGINA - MARGEN - ancho, 15);
                pie.showText(etiqueta);
                pie.endText();
            }
        }
    }

    private void agregarTablaEvaluaciones(PdfBuilder pdf, List<EvaluacionOrdenMeritoDTO> evaluaciones) throws IOException {
        String[] encabezados = {"N°", "Cuestionario", "Candidato", "Fecha", "Estado"};
        List<String[]> filas = new ArrayList<>();
        int numero = 1;
        for (EvaluacionOrdenMeritoDTO evaluacion : evaluaciones) {
            filas.add(new String[]{String.valueOf(numero++), String.valueOf(evaluacion.id()), evaluacion.proyecto(),
                    evaluacion.fecha(), evaluacion.estado()});
        }
        pdf.agregarTabla("Evaluaciones incluidas", encabezados, filas, new float[]{0.05f, 0.12f, 0.42f, 0.20f, 0.21f});
    }

    private void agregarTablaCandidatos(PdfBuilder pdf, String titulo, List<CandidatoOrdenMeritoDTO> candidatos) throws IOException {
        String[] encabezados = {"Orden", "Cuestionario", "Documento", "Candidato", "Estado cuestionario", "Puntaje", "Estado de mérito", "Inicio", "Finalización", "Datos adicionales"};
        List<String[]> filas = new ArrayList<>();
        int orden = 1;
        for (CandidatoOrdenMeritoDTO candidato : candidatos) {
            String puntaje = candidato.puntajeObtenido() == null ? "Pendiente" : String.format(java.util.Locale.ROOT, "%.2f", candidato.puntajeObtenido());
            filas.add(new String[]{
                    "APTO".equals(candidato.estadoMerito()) ? String.valueOf(orden++) : "-",
                    String.valueOf(candidato.cuestionarioId()),
                    texto(candidato.tipoDocumento()) + " " + texto(candidato.nroDocumento()),
                    texto(candidato.apellido()) + ", " + texto(candidato.nombre()),
                    texto(candidato.estadoCuestionario()).replace('_', ' '), puntaje,
                    estadoMerito(candidato.estadoMerito()), fecha(candidato.fechaInicio()), fecha(candidato.fechaFinalizacion()),
                    texto(candidato.datosAdicionales())
            });
        }
        pdf.agregarTabla(titulo, encabezados, filas, PESOS_COLUMNAS);
    }

    private List<CandidatoOrdenMeritoDTO> filtrar(List<CandidatoOrdenMeritoDTO> candidatos, String estado) {
        return candidatos.stream().filter(c -> estado.equals(c.estadoMerito())).toList();
    }

    private String estadoMerito(String estado) {
        return switch (estado) {
            case "APTO" -> "Apto";
            case "NO_ALCANZA_MINIMOS" -> "No alcanza los mínimos";
            case "PENDIENTE_PUNTAJE" -> "Puntaje pendiente";
            default -> "Cuestionario incompleto";
        };
    }

    private String fecha(java.time.LocalDateTime fecha) { return fecha == null ? "-" : FECHA_HORA.format(fecha); }
    private static String texto(String valor) { return valor == null || valor.isBlank() ? "-" : valor; }

    private static final class PdfBuilder {
        private final PDDocument documento;
        private final InformeOrdenMeritoDTO informe;
        private PDPageContentStream contenido;
        private float y;

        private PdfBuilder(PDDocument documento, InformeOrdenMeritoDTO informe) {
            this.documento = documento;
            this.informe = informe;
        }

        private void nuevaPagina(boolean primera) throws IOException {
            cerrarPagina();
            PDPage pagina = new PDPage(new PDRectangle(ANCHO_PAGINA, ALTO_PAGINA));
            documento.addPage(pagina);
            contenido = new PDPageContentStream(documento, pagina);
            y = ALTO_PAGINA - MARGEN;
            if (!primera) {
                agregarTexto("ORDEN DE MÉRITO - " + GeneradorInformePdf.texto(informe.codigoPuesto()), 9, true);
                y -= 4;
            }
        }

        private void cerrarPagina() throws IOException {
            if (contenido != null) {
                contenido.close();
                contenido = null;
            }
        }

        private void agregarTextoCentrado(String texto, float tamanio, boolean negrita) throws IOException {
            PDType1Font fuente = negrita ? NEGRITA : NORMAL;
            float ancho = fuente.getStringWidth(pdfSafe(texto)) / 1000 * tamanio;
            texto(texto, (ANCHO_PAGINA - ancho) / 2, y, tamanio, negrita);
            y -= tamanio + 9;
        }

        private void agregarTexto(String texto, float tamanio, boolean negrita) throws IOException {
            texto(texto, MARGEN, y, tamanio, negrita);
            y -= tamanio + 5;
        }

        private void texto(String texto, float x, float baseline, float tamanio, boolean negrita) throws IOException {
            contenido.setNonStrokingColor(Color.BLACK);
            contenido.beginText();
            contenido.setFont(negrita ? NEGRITA : NORMAL, tamanio);
            contenido.newLineAtOffset(x, baseline);
            contenido.showText(pdfSafe(texto));
            contenido.endText();
        }

        private void agregarTabla(String titulo, String[] encabezados, List<String[]> filas, float[] pesos) throws IOException {
            if (y < 130) nuevaPagina(false);
            texto(titulo, MARGEN, y, 10, true);
            y -= 16;
            if (filas.isEmpty()) {
                texto(MENSAJE_SIN_DATOS, MARGEN + 4, y, 8, false);
                y -= 17;
                return;
            }
            float[] anchos = new float[pesos.length];
            for (int i = 0; i < pesos.length; i++) anchos[i] = ANCHO_TABLA * pesos[i];
            dibujarFila(encabezados, anchos, true);
            for (String[] fila : filas) {
                List<String[]> lineas = new ArrayList<>();
                int maxLineas = 1;
                for (int i = 0; i < fila.length; i++) {
                    String[] envueltas = envolver(fila[i], anchos[i] - 6, 7);
                    lineas.add(envueltas);
                    maxLineas = Math.max(maxLineas, envueltas.length);
                }
                float alto = maxLineas * 9 + 6;
                if (y - alto < MARGEN + 12) {
                    nuevaPagina(false);
                    texto(titulo + " (continuación)", MARGEN, y, 10, true);
                    y -= 16;
                    dibujarFila(encabezados, anchos, true);
                }
                dibujarFila(lineas, anchos, alto);
            }
            y -= 10;
        }

        private void dibujarFila(String[] valores, float[] anchos, boolean encabezado) throws IOException {
            List<String[]> lineas = new ArrayList<>();
            int maxLineas = 1;
            for (int i = 0; i < valores.length; i++) {
                String[] envueltas = envolver(valores[i], anchos[i] - 6, 7);
                lineas.add(envueltas);
                maxLineas = Math.max(maxLineas, envueltas.length);
            }
            dibujarFila(lineas, anchos, maxLineas * 9 + 7, encabezado);
        }

        private void dibujarFila(List<String[]> lineas, float[] anchos, float alto) throws IOException {
            dibujarFila(lineas, anchos, alto, false);
        }

        private void dibujarFila(List<String[]> lineas, float[] anchos, float alto, boolean encabezado) throws IOException {
            float x = MARGEN;
            for (int i = 0; i < anchos.length; i++) {
                contenido.setNonStrokingColor(encabezado ? new Color(210, 210, 210) : new Color(245, 245, 245));
                contenido.addRect(x, y - alto, anchos[i], alto);
                contenido.fill();
                contenido.setStrokingColor(new Color(125, 125, 125));
                contenido.addRect(x, y - alto, anchos[i], alto);
                contenido.stroke();
                String[] textoCelda = lineas.get(i);
                for (int l = 0; l < textoCelda.length; l++) {
                    texto(textoCelda[l], x + 3, y - 10 - (l * 9), 7, encabezado);
                }
                x += anchos[i];
            }
            y -= alto;
        }

        private String[] envolver(String valor, float anchoDisponible, float tamanio) throws IOException {
            String texto = pdfSafe(valor == null ? "-" : valor);
            List<String> resultado = new ArrayList<>();
            StringBuilder linea = new StringBuilder();
            for (String palabra : texto.split("\\s+")) {
                String propuesta = linea.isEmpty() ? palabra : linea + " " + palabra;
                if (NORMAL.getStringWidth(propuesta) / 1000 * tamanio <= anchoDisponible) {
                    linea.setLength(0);
                    linea.append(propuesta);
                } else if (linea.isEmpty()) {
                    resultado.add(palabra.substring(0, Math.min(12, palabra.length())));
                    linea.append(palabra.substring(Math.min(12, palabra.length())));
                } else {
                    resultado.add(linea.toString());
                    linea.setLength(0);
                    linea.append(palabra);
                }
            }
            if (!linea.isEmpty()) resultado.add(linea.toString());
            return resultado.isEmpty() ? new String[]{"-"} : resultado.toArray(String[]::new);
        }

        private String pdfSafe(String texto) {
            return texto.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
        }
    }
}
