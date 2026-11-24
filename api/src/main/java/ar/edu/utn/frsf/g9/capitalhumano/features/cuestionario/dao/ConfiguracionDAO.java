package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * DAO del repositorio de configuración de los cuestionarios (CU 026).
 */
public interface ConfiguracionDAO extends JpaRepository<Configuracion, Long> {

    Optional<Configuracion> findFirstByOrderByIdAsc();

    /**
     * Obtiene la configuración vigente. Si no hay ninguna cargada se usan valores por defecto.
     *
     * @return configuración vigente
     */
    default Configuracion obtener() {
        return findFirstByOrderByIdAsc().orElseGet(() -> new Configuracion(
                "Responda todas las preguntas de cada bloque. Una vez que avance al bloque siguiente no podrá volver atrás.",
                60, 168, 5, 2));
    }
}
