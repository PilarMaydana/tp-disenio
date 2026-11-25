package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tarea programada que cierra los cuestionarios vencidos (CU 026 - flujos 1.A y 1.B).
 *
 * <p>Se ejecuta al iniciar la aplicación y luego cada
 * {@code capitalhumano.cuestionarios.cierre-vencidos.intervalo-ms} milisegundos (por defecto 5 minutos).
 * Puede deshabilitarse con {@code capitalhumano.cuestionarios.cierre-vencidos.habilitado=false}.</p>
 */
@Component
@ConditionalOnProperty(name = "capitalhumano.cuestionarios.cierre-vencidos.habilitado",
        havingValue = "true", matchIfMissing = true)
public class CierreCuestionariosVencidosJob {

    private final GestorCuestionario gestorCuestionario;

    public CierreCuestionariosVencidosJob(GestorCuestionario gestorCuestionario) {
        this.gestorCuestionario = gestorCuestionario;
    }

    @Scheduled(fixedDelayString = "${capitalhumano.cuestionarios.cierre-vencidos.intervalo-ms:300000}")
    public void cerrarVencidos() {
        gestorCuestionario.finalizarCuestionariosVencidos();
    }
}
