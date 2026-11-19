package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * DAO para el acceso a datos de la entidad {@link Evaluacion} (CU 025).
 */
public interface EvaluacionDAO extends JpaRepository<Evaluacion, Long> {

    /**
     * Obtiene una evaluación junto con su puesto, cuestionarios y candidatos.
     *
     * @param id identificador de la evaluación
     * @return evaluación encontrada o vacío
     */
    @Query("SELECT DISTINCT e FROM Evaluacion e " +
           "JOIN FETCH e.puesto p " +
           "LEFT JOIN FETCH e.cuestionarios q " +
           "LEFT JOIN FETCH q.candidato c " +
           "WHERE e.id = :id")
    Optional<Evaluacion> obtenerPorId(@Param("id") Long id);
}
