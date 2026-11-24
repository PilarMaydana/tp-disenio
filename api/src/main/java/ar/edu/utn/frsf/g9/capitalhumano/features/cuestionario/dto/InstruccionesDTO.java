package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto;

/**
 * Pantalla "Cuestionario - Instrucciones" (CU 026 - mensajes 2 a 4).
 *
 * @param instrucciones              texto de las instrucciones (repositorio de configuración)
 * @param tiempoMaximoMinutos        tiempo máximo para completar el cuestionario
 * @param cantidadPreguntasPorBloque preguntas por paso del wizard
 * @param cantidadMaximaAccesos      ingresos permitidos
 */
public record InstruccionesDTO(
        String instrucciones,
        Integer tiempoMaximoMinutos,
        Integer cantidadPreguntasPorBloque,
        Integer cantidadMaximaAccesos
) {
}
