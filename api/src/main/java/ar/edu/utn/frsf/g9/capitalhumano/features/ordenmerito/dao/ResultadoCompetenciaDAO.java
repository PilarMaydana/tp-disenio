package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.model.ResultadoCompetencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultadoCompetenciaDAO extends JpaRepository<ResultadoCompetencia, Long> {
    List<ResultadoCompetencia> findByCuestionarioIdIn(List<Long> cuestionarioIds);
    Optional<ResultadoCompetencia> findByCuestionarioIdAndCompetenciaId(Long cuestionarioId, Long competenciaId);
}
