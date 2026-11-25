package ar.edu.utn.frsf.g9.capitalhumano.features.auth.dao;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para el acceso a datos de la entidad {@link Candidato}.
 */
@Repository
public interface CandidatoRepository extends JpaRepository<Candidato, Long> {

    /**
     * Busca un candidato vigente (no dado de baja lógica) por tipo y número de documento.
     *
     * @param tipo tipo de documento
     * @param nroDocumento número de documento
     * @return candidato encontrado o Optional.empty()
     */
    Optional<Candidato> findByTipoDocumentoAndNroDocumentoAndEliminadoFalse(TipoDocumento tipo, String nroDocumento);
}
