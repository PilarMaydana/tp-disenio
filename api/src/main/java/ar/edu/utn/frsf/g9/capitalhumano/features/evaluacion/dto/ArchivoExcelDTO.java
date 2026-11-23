package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

/**
 * Archivo Excel generado con los candidatos de una evaluación y sus claves (CU 025 - paso 17).
 *
 * @param nombreArchivo nombre con formato {@code aaaa-mm-dd-hh-mm-NombreFunción.xls}
 * @param contenido     bytes del archivo
 */
public record ArchivoExcelDTO(
        String nombreArchivo,
        byte[] contenido
) {
}
