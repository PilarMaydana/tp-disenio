package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.gestor.GestorCandidatos;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.CuestionarioWizardDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dto.RespuestaBloqueDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor.GestorCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao.EvaluacionDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.CandidatoClaveDTO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto.ConfirmarEvaluacionRequestDTO;
import ar.edu.utn.frsf.g9.capitalhumano.shared.exception.ReglaNegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
 * CU 026 - Pedidos simultáneos sobre el mismo cuestionario (doble click en «Aceptar» o en
 * «Siguiente»/«Finalizar»).
 *
 * <p>No es transaccional a propósito: cada pedido corre en su propio hilo y transacción, como
 * dos pedidos HTTP reales. Se usa un cuestionario nuevo y al terminar se borra.</p>
 */
@SpringBootTest
class CuestionarioConcurrenciaTest {

    /** Candidato del seed sin cuestionarios (no lo usa ningún otro test). */
    private static final long CANDIDATO = 8L;
    private static final int ENVIOS = 3;

    @Autowired
    private GestorCandidatos gestorCandidatos;

    @Autowired
    private GestorCuestionario gestorCuestionario;

    @Autowired
    private EvaluacionDAO evaluacionDAO;

    private Long evaluacionId;

    @BeforeEach
    void crearCuestionario() {
        evaluacionId = gestorCandidatos.evaluarCandidatos(new ConfirmarEvaluacionRequestDTO("BACK-01",
                List.of(new CandidatoClaveDTO(CANDIDATO, null, null, null, null, null)))).evaluacionId();
    }

    @AfterEach
    void limpiar() {
        evaluacionDAO.deleteById(evaluacionId);
    }

    @Test
    @DisplayName("Doble «Aceptar» en el primer ingreso: los bloques se arman una sola vez")
    void inicioSimultaneo() throws Exception {
        List<CuestionarioWizardDTO> pasos = enParalelo(() -> gestorCuestionario.iniciarOReanudarCuestionario(CANDIDATO));

        assertThat(pasos).hasSize(ENVIOS);
        // Todos ven el mismo cuestionario con las mismas preguntas del bloque 1
        assertThat(pasos).extracting(CuestionarioWizardDTO::totalBloques).containsOnly(2);
        assertThat(pasos).extracting(CuestionarioWizardDTO::totalPreguntas).containsOnly(4);
        assertThat(pasos).extracting(p -> p.preguntas().get(0).preguntaAsignadaId())
                .containsOnly(pasos.get(0).preguntas().get(0).preguntaAsignadaId());
    }

    @Test
    @DisplayName("Doble envío del mismo bloque: se guarda una vez y los demás se rechazan (no error 500)")
    void respuestaSimultanea() throws Exception {
        CuestionarioWizardDTO paso = gestorCuestionario.iniciarOReanudarCuestionario(CANDIDATO);
        List<RespuestaBloqueDTO> respuestas = paso.preguntas().stream()
                .map(p -> new RespuestaBloqueDTO(p.preguntaAsignadaId(), p.opciones().get(0).id()))
                .toList();

        List<CuestionarioWizardDTO> aceptados = enParalelo(() -> gestorCuestionario.responderBloque(
                CANDIDATO, paso.cuestionarioId(), paso.numeroBloque(), respuestas));

        assertThat(aceptados).hasSize(1);
        assertThat(aceptados.get(0).numeroBloque()).isEqualTo(2);
        assertThat(aceptados.get(0).preguntasRespondidas()).isEqualTo(2);
    }

    /**
     * Ejecuta el mismo pedido {@value #ENVIOS} veces a la vez. Devuelve los resultados exitosos;
     * los rechazos solo pueden ser por regla de negocio (nunca un error interno).
     */
    private List<CuestionarioWizardDTO> enParalelo(Callable<CuestionarioWizardDTO> pedido) throws Exception {
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(ENVIOS);
        List<Future<CuestionarioWizardDTO>> futuros = new ArrayList<>();
        for (int i = 0; i < ENVIOS; i++) {
            futuros.add(hilos.submit(() -> {
                largada.await();
                return pedido.call();
            }));
        }
        largada.countDown();

        List<CuestionarioWizardDTO> exitosos = new ArrayList<>();
        for (Future<CuestionarioWizardDTO> futuro : futuros) {
            try {
                exitosos.add(futuro.get(30, TimeUnit.SECONDS));
            } catch (ExecutionException e) {
                assertThat(e.getCause()).isInstanceOf(ReglaNegocioException.class);
            }
        }
        hilos.shutdown();
        return exitosos;
    }
}
