package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.EstadoCuestionario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * DAO para el acceso a datos de la entidad {@link Cuestionario}.
 */
public interface CuestionarioDAO extends JpaRepository<Cuestionario, Long> {

    /** Estados en los que el cuestionario todavía puede responderse. */
    List<EstadoCuestionario> ESTADOS_ABIERTOS = List.of(EstadoCuestionario.ACTIVO, EstadoCuestionario.EN_PROCESO);

    /**
     * Verifica si el candidato tiene algún cuestionario vigente en alguno de los estados indicados.
     *
     * @param nroCandidato número de candidato
     * @param estados      estados a considerar
     * @return {@code true} si existe al menos uno
     */
    boolean existsByCandidatoNroCandidatoAndEstadoInAndEliminadoFalse(Long nroCandidato,
                                                                      List<EstadoCuestionario> estados);

    Optional<Cuestionario> findFirstByCandidatoNroCandidatoAndEstadoInAndEliminadoFalseOrderByIdDesc(
            Long nroCandidato, List<EstadoCuestionario> estados);

    /**
     * Indica si el candidato tiene un cuestionario ACTIVO o EN_PROCESO (CU 025 - flujo 6.A).
     *
     * @param nroCandidato número de candidato
     * @return {@code true} si tiene un cuestionario activo o en proceso
     */
    default boolean tieneCuestionarioActivoOProceso(Long nroCandidato) {
        return existsByCandidatoNroCandidatoAndEstadoInAndEliminadoFalse(nroCandidato, ESTADOS_ABIERTOS);
    }

    /**
     * Obtiene el cuestionario ACTIVO o EN_PROCESO del candidato (CU 026 - mensaje 9).
     * Un candidato puede tener un solo cuestionario abierto a la vez.
     *
     * @param nroCandidato número de candidato
     * @return cuestionario abierto, o vacío si no tiene
     */
    default Optional<Cuestionario> obtenerActivoOEnProceso(Long nroCandidato) {
        return findFirstByCandidatoNroCandidatoAndEstadoInAndEliminadoFalseOrderByIdDesc(nroCandidato, ESTADOS_ABIERTOS);
    }

    List<Cuestionario> findByEstadoInAndEliminadoFalse(List<EstadoCuestionario> estados);

    /**
     * Obtiene el cuestionario bloqueando su fila hasta el fin de la transacción (CU 026).
     *
     * <p>Serializa los pedidos simultáneos sobre el mismo cuestionario (doble click en
     * «Aceptar» o en «Siguiente»/«Finalizar»): el segundo espera al primero y luego lee el
     * estado ya actualizado, en lugar de armar los bloques o guardar las respuestas dos veces.</p>
     *
     * @param id identificador del cuestionario
     * @return cuestionario encontrado o vacío
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cuestionario c WHERE c.id = :id")
    Optional<Cuestionario> bloquearPorId(@Param("id") Long id);

    @Query("SELECT c.id FROM Cuestionario c WHERE c.candidato.nroCandidato = :nroCandidato "
            + "AND c.estado IN :estados AND c.eliminado = false ORDER BY c.id DESC")
    List<Long> buscarIdsPorCandidatoYEstado(@Param("nroCandidato") Long nroCandidato,
                                            @Param("estados") List<EstadoCuestionario> estados);

    /**
     * Obtiene y bloquea el cuestionario ACTIVO o EN_PROCESO del candidato (CU 026 - mensaje 9).
     *
     * <p>Primero se busca solo el id y luego se lee la fila con bloqueo, para que la entidad
     * se cargue después de obtener el bloqueo y refleje los cambios de un pedido anterior.</p>
     *
     * @param nroCandidato número de candidato
     * @return cuestionario abierto bloqueado, o vacío si no tiene
     */
    default Optional<Cuestionario> bloquearActivoOEnProceso(Long nroCandidato) {
        return buscarIdsPorCandidatoYEstado(nroCandidato, ESTADOS_ABIERTOS).stream()
                .findFirst()
                .flatMap(this::bloquearPorId)
                .filter(c -> ESTADOS_ABIERTOS.contains(c.getEstado()));
    }

    /**
     * Obtiene los cuestionarios ACTIVO o EN_PROCESO, candidatos a cerrarse por vencimiento
     * (diagrama de clases - {@code finalizarVencidos()}).
     *
     * @return cuestionarios abiertos
     */
    default List<Cuestionario> obtenerAbiertos() {
        return findByEstadoInAndEliminadoFalse(ESTADOS_ABIERTOS);
    }
}
