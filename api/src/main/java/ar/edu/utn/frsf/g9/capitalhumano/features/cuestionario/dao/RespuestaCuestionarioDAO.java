package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.RespuestaCuestionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RespuestaCuestionarioDAO extends JpaRepository<RespuestaCuestionario, Long> {
    List<RespuestaCuestionario> findByCuestionarioId(Long cuestionarioId);
    Optional<RespuestaCuestionario> findByCuestionarioIdAndPreguntaId(Long cuestionarioId, Long preguntaId);
}
