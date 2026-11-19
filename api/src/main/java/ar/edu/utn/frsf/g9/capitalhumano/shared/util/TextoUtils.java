package ar.edu.utn.frsf.g9.capitalhumano.shared.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Utilidades para comparar textos ignorando mayúsculas, espacios y acentos.
 */
public final class TextoUtils {

    private TextoUtils() {
    }

    /**
     * Normaliza un texto: quita espacios extremos, acentos y lo pasa a minúsculas.
     *
     * @param texto texto a normalizar (puede ser {@code null})
     * @return texto normalizado, o cadena vacía si es {@code null}
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase(Locale.ROOT);
    }

    /**
     * Indica si {@code texto} contiene a {@code filtro}, ignorando mayúsculas y acentos.
     * Un filtro vacío o {@code null} se considera siempre coincidente.
     *
     * @param texto  texto donde buscar
     * @param filtro filtro ingresado por el usuario
     * @return {@code true} si coincide
     */
    public static boolean contieneIgnorandoAcentos(String texto, String filtro) {
        String f = normalizar(filtro);
        return f.isEmpty() || normalizar(texto).contains(f);
    }
}
