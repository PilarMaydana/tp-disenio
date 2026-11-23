package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.gestor;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao.EvaluacionDAO;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Gestor de lógica de negocio de los Cuestionarios.
 *
 * <p>En el CU 025 se encarga de generar la {@link Evaluacion} con un cuestionario en estado
 * ACTIVO por cada candidato. La selección aleatoria de preguntas se realiza recién cuando el
 * candidato ingresa a completar el cuestionario (CU 026 - paso 3).</p>
 */
@Service
public class GestorCuestionario {

    private static final Logger log = LoggerFactory.getLogger(GestorCuestionario.class);

    private final EvaluacionDAO evaluacionDAO;

    public GestorCuestionario(EvaluacionDAO evaluacionDAO) {
        this.evaluacionDAO = evaluacionDAO;
    }

    /**
     * Genera y persiste una evaluación para el puesto con un cuestionario ACTIVO por candidato
     * (CU 025 - mensajes 57 a 60).
     *
     * <p>Todos los cuestionarios de la evaluación referencian al mismo puesto, por lo que se basan
     * en el mismo modelo de competencias, factores, preguntas y opciones de respuesta.</p>
     *
     * @param candidatosConClave candidatos a evaluar con la clave de acceso asignada a cada uno
     *                           (se respeta el orden de inserción)
     * @param puesto             puesto o función para el cual se evalúa
     * @param usuarioId          consultor que genera la evaluación
     * @return evaluación guardada con sus cuestionarios
     */
    @Transactional
    public Evaluacion generarCuestionariosParaEvaluacion(Map<Candidato, String> candidatosConClave,
                                                         Puesto puesto,
                                                         Long usuarioId) {
        LocalDateTime ahora = LocalDateTime.now();
        Evaluacion evaluacion = new Evaluacion(puesto, ahora, usuarioId);

        candidatosConClave.forEach((candidato, clave) -> {
            Cuestionario cuestionario = new Cuestionario();
            cuestionario.setCandidato(candidato);
            cuestionario.setPuestoId(puesto.getId());
            cuestionario.setEstado(EstadoCuestionario.ACTIVO);
            cuestionario.setClaveAcceso(clave);
            cuestionario.setFechaCreacion(ahora);
            cuestionario.setEliminado(false);
            evaluacion.agregarCuestionario(cuestionario);
        });

        Evaluacion guardada = evaluacionDAO.save(evaluacion);
        log.info("Evaluación {} creada para el puesto {} con {} cuestionario(s)",
                guardada.getId(), puesto.getCodigo(), guardada.getCuestionarios().size());
        return guardada;
    }
}
