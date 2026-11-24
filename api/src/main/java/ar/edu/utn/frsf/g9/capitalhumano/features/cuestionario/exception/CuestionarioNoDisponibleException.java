package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.exception;

import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.CapitalHumanoException;

/**
 * El cuestionario no puede completarse: se superó el tiempo máximo o la cantidad de accesos
 * (CU 026 - flujos 1.A y 1.B). El cambio de estado ya quedó registrado.
 */
public class CuestionarioNoDisponibleException extends CapitalHumanoException {

    public static final String INCOMPLETO = "CUESTIONARIO_INCOMPLETO";
    public static final String SIN_CONTESTAR = "CUESTIONARIO_SIN_CONTESTAR";

    public CuestionarioNoDisponibleException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
