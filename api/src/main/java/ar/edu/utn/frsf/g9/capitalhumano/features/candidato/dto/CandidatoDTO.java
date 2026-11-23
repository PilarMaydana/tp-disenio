package ar.edu.utn.frsf.g9.capitalhumano.features.candidato.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;

/**
 * DTO con los datos de un candidato que se muestran en la grilla de búsqueda (CU 025 - paso 3).
 *
 * @param id            identificador interno
 * @param nroCandidato  número de candidato
 * @param apellido      apellido
 * @param nombre        nombre
 * @param tipoDocumento tipo de documento
 * @param nroDocumento  número de documento
 */
public record CandidatoDTO(
        Long id,
        Long nroCandidato,
        String apellido,
        String nombre,
        TipoDocumento tipoDocumento,
        String nroDocumento
) {

    /**
     * Construye el DTO a partir de la entidad.
     *
     * @param c candidato
     * @return DTO equivalente
     */
    public static CandidatoDTO desde(Candidato c) {
        return new CandidatoDTO(c.getId(), c.getNroCandidato(), c.getApellido(), c.getNombre(),
                c.getTipoDocumento(), c.getNroDocumento());
    }
}
