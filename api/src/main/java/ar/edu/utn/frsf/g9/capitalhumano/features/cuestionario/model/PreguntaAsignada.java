package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Pregunta elegida al azar para un cuestionario (CU 026 - paso 3). Guarda el orden en que se
 * presenta, para que en un reingreso el cuestionario se muestre de forma idéntica.
 */
@Entity
@Table(name = "pregunta_asignada")
public class PreguntaAsignada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bloque_id", nullable = false)
    private BloqueCuestionario bloque;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    @Column(name = "nro_orden", nullable = false)
    private Integer nroOrden;

    @OneToOne(mappedBy = "preguntaAsignada", cascade = CascadeType.ALL, orphanRemoval = true)
    private RespuestaCandidato respuesta;

    public PreguntaAsignada() {
    }

    public PreguntaAsignada(BloqueCuestionario bloque, Pregunta pregunta, Integer nroOrden) {
        this.bloque = bloque;
        this.pregunta = pregunta;
        this.nroOrden = nroOrden;
    }

    /**
     * Registra la opción elegida por el candidato.
     *
     * @param opcion opción elegida
     * @param ahora  fecha y hora de la respuesta
     */
    public void responder(OpcionRespuesta opcion, LocalDateTime ahora) {
        this.respuesta = new RespuestaCandidato(this, opcion, ahora);
    }

    public boolean fueRespondida() {
        return respuesta != null;
    }

    public Long getId() {
        return id;
    }

    public BloqueCuestionario getBloque() {
        return bloque;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public Integer getNroOrden() {
        return nroOrden;
    }

    public RespuestaCandidato getRespuesta() {
        return respuesta;
    }
}
