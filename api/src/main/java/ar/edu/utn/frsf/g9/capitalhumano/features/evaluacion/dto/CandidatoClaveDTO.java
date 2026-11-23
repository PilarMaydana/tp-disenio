package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.dto;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.TipoDocumento;
import jakarta.validation.constraints.NotNull;

/**
 * DTO con los datos de un candidato a evaluar y su clave de ingreso (CU 025 - paso 11).
 *
 * @param nroCandidato  número de candidato
 * @param apellido      apellido
 * @param nombre        nombre
 * @param tipoDocumento tipo de documento
 * @param nroDocumento  número de documento
 * @param claveAcceso   clave de ingreso al cuestionario (8 caracteres alfanuméricos)
 */
public record CandidatoClaveDTO(
        @NotNull(message = "El número de candidato es obligatorio")
        Long nroCandidato,
        String apellido,
        String nombre,
        TipoDocumento tipoDocumento,
        String nroDocumento,
        String claveAcceso
) {

    /**
     * Construye el DTO a partir del candidato y su clave.
     *
     * @param c     candidato
     * @param clave clave de ingreso
     * @return DTO equivalente
     */
    public static CandidatoClaveDTO desde(Candidato c, String clave) {
        return new CandidatoClaveDTO(c.getNroCandidato(), c.getApellido(), c.getNombre(),
                c.getTipoDocumento(), c.getNroDocumento(), clave);
    }
}
