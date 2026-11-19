package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * DAO para el acceso a datos de la entidad {@link Cuestionario}.
 */
public interface CuestionarioDAO extends JpaRepository<Cuestionario, Long> {

    /**
     * Verifica si el candidato tiene algún cuestionario vigente en alguno de los estados indicados.
     *
     * @param nroCandidato número de candidato
     * @param estados      estados a considerar
     * @return {@code true} si existe al menos uno
     */
    boolean existsByCandidatoNroCandidatoAndEstadoInAndEliminadoFalse(Long nroCandidato,
                                                                      List<EstadoCuestionario> estados);

    /**
     * Indica si el candidato tiene un cuestionario ACTIVO o EN_PROCESO (CU 025 - flujo 6.A).
     *
     * @param nroCandidato número de candidato
     * @return {@code true} si tiene un cuestionario activo o en proceso
     */
    default boolean tieneCuestionarioActivoOProceso(Long nroCandidato) {
        return existsByCandidatoNroCandidatoAndEstadoInAndEliminadoFalse(
                nroCandidato, List.of(EstadoCuestionario.ACTIVO, EstadoCuestionario.EN_PROCESO));
    }
}
