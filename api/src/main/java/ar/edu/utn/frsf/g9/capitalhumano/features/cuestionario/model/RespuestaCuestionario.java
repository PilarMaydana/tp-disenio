package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.OpcionRespuesta;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Opción seleccionada por un candidato para una pregunta de su cuestionario. */
@Entity
@Table(name = "respuesta_cuestionario", uniqueConstraints = @UniqueConstraint(
        name = "uk_respuesta_cuestionario_pregunta", columnNames = {"cuestionario_id", "pregunta_id"}))
public class RespuestaCuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuestionario_id", nullable = false)
    private Cuestionario cuestionario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opcion_respuesta_id", nullable = false)
    private OpcionRespuesta opcion;

    protected RespuestaCuestionario() { }

    public RespuestaCuestionario(Cuestionario cuestionario, Pregunta pregunta, OpcionRespuesta opcion) {
        this.cuestionario = cuestionario;
        this.pregunta = pregunta;
        this.opcion = opcion;
    }

    public Long getId() { return id; }
    public Cuestionario getCuestionario() { return cuestionario; }
    public Pregunta getPregunta() { return pregunta; }
    public OpcionRespuesta getOpcion() { return opcion; }
    public void setOpcion(OpcionRespuesta opcion) { this.opcion = opcion; }
}
