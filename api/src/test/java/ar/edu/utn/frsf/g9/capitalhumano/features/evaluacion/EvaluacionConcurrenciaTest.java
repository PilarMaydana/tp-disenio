package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.gestor.GestorCandidatos;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao.CuestionarioDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao.EvaluacionDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CandidatoClaveDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ConfirmarEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.EvaluacionCreadaDTO;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CU 025 - Confirmaciones simultáneas de una evaluación (por ejemplo, doble click en «Finalizar»).
 *
 * <p>No es transaccional a propósito: cada confirmación corre en su propio hilo y transacción,
 * como dos pedidos HTTP reales. Al terminar se borran las evaluaciones creadas.</p>
 */
@SpringBootTest
class EvaluacionConcurrenciaTest {

    /** Candidato del seed sin cuestionarios (no lo usa ningún otro test). */
    private static final long CANDIDATO = 7L;
    private static final int ENVIOS = 3;

    @Autowired
    private GestorCandidatos gestorCandidatos;

    @Autowired
    private CuestionarioDAO cuestionarioDAO;

    @Autowired
    private EvaluacionDAO evaluacionDAO;

    @Autowired
    private TransactionTemplate transaccion;

    private final List<Long> evaluacionesCreadas = new ArrayList<>();

    @AfterEach
    void limpiar() {
        evaluacionesCreadas.forEach(evaluacionDAO::deleteById);
    }

    @Test
    @DisplayName("Confirmaciones simultáneas: se crea un solo cuestionario y las demás se rechazan por el 6.A")
    void dobleEnvioCreaUnSoloCuestionario() throws Exception {
        ConfirmarEvaluacionRequestDTO request = new ConfirmarEvaluacionRequestDTO("BACK-01",
                List.of(new CandidatoClaveDTO(CANDIDATO, null, null, null, null, "Clave123")));

        CountDownLatch largada = new CountDownLatch(1);
        Callable<EvaluacionCreadaDTO> confirmar = () -> {
            largada.await();
            return gestorCandidatos.evaluarCandidatos(request);
        };

        ExecutorService hilos = Executors.newFixedThreadPool(ENVIOS);
        List<Future<EvaluacionCreadaDTO>> envios = new ArrayList<>();
        for (int i = 0; i < ENVIOS; i++) {
            envios.add(hilos.submit(confirmar));
        }
        largada.countDown();

        int rechazados = 0;
        for (Future<EvaluacionCreadaDTO> envio : envios) {
            try {
                evaluacionesCreadas.add(envio.get(30, TimeUnit.SECONDS).evaluacionId());
            } catch (ExecutionException e) {
                assertThat(e.getCause()).isInstanceOf(ReglaNegocioException.class)
                        .hasMessageContaining("cuestionario activo o en proceso");
                rechazados++;
            }
        }
        hilos.shutdown();

        assertThat(evaluacionesCreadas).hasSize(1);
        assertThat(rechazados).isEqualTo(ENVIOS - 1);

        long abiertos = transaccion.execute(estado -> cuestionarioDAO.findAll().stream()
                .filter(c -> c.getCandidato().getNroCandidato().equals(CANDIDATO))
                .filter(c -> CuestionarioDAO.ESTADOS_ABIERTOS.contains(c.getEstado()))
                .count());
        assertThat(abiertos).isEqualTo(1);
    }
}
