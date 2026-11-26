package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CuestionarioOrdenMeritoDAO extends JpaRepository<Cuestionario, Long> {
    List<Cuestionario> findByPuestoIdAndEliminadoFalseOrderByIdAsc(Long puestoId);
}
