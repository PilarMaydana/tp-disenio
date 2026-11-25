package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.shared.util.TextoUtils;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * DAO para el acceso a datos de la entidad {@link Candidato} (CU 025).
 */
public interface CandidatoDAO extends JpaRepository<Candidato, Long> {

    /**
     * Obtiene un candidato vigente por su número de candidato.
     *
     * @param nroCandidato número de candidato
     * @return candidato encontrado o vacío
     */
    Optional<Candidato> findByNroCandidatoAndEliminadoFalse(Long nroCandidato);

    /**
     * Obtiene el candidato bloqueando su fila hasta el fin de la transacción (CU 025 - paso 13).
     *
     * <p>Evita que dos confirmaciones simultáneas (por ejemplo, un doble click en «Finalizar»)
     * pasen a la vez el control del flujo 6.A y le generen dos cuestionarios activos al mismo
     * candidato: la segunda espera a que termine la primera y luego encuentra el cuestionario.</p>
     *
     * @param nroCandidato número de candidato
     * @return candidato encontrado o vacío
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Candidato c WHERE c.nroCandidato = :nroCandidato AND c.eliminado = false")
    Optional<Candidato> bloquearPorNroCandidato(@Param("nroCandidato") Long nroCandidato);

    /**
     * Lista todos los candidatos vigentes ordenados por apellido y nombre.
     *
     * @return candidatos no eliminados
     */
    List<Candidato> findByEliminadoFalseOrderByApellidoAscNombreAsc();

    /**
     * Busca candidatos vigentes según los filtros del CU 025 (paso 3).
     *
     * <p>El número de candidato se compara de forma exacta; apellido y nombre se comparan
     * de forma parcial ignorando mayúsculas y acentos. Los filtros vacíos se ignoran.</p>
     *
     * @param apellido     filtro de apellido (opcional)
     * @param nombre       filtro de nombre (opcional)
     * @param nroCandidato filtro de número de candidato (opcional)
     * @return candidatos que cumplen los criterios
     */
    default List<Candidato> buscar(String apellido, String nombre, Long nroCandidato) {
        List<Candidato> base = nroCandidato != null
                ? findByNroCandidatoAndEliminadoFalse(nroCandidato).map(List::of).orElse(List.of())
                : findByEliminadoFalseOrderByApellidoAscNombreAsc();
        return base.stream()
                .filter(c -> TextoUtils.contieneIgnorandoAcentos(c.getApellido(), apellido))
                .filter(c -> TextoUtils.contieneIgnorandoAcentos(c.getNombre(), nombre))
                .toList();
    }
}
