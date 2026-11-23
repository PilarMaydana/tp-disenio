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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bloque de preguntas de un cuestionario: corresponde a un paso del wizard (CU 026).
 * Una vez respondido no puede volver a modificarse.
 */
@Entity
@Table(name = "bloque_cuestionario")
public class BloqueCuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuestionario_id", nullable = false)
    private Cuestionario cuestionario;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    @Column(name = "completado", nullable = false)
    private Boolean completado = Boolean.FALSE;

    @OneToMany(mappedBy = "bloque", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("nroOrden ASC")
    private List<PreguntaAsignada> preguntas = new ArrayList<>();

    public BloqueCuestionario() {
    }

    public BloqueCuestionario(Cuestionario cuestionario, Integer numero) {
        this.cuestionario = cuestionario;
        this.numero = numero;
    }

    /**
     * Agrega una pregunta al bloque.
     *
     * @param pregunta pregunta seleccionada al azar
     * @param nroOrden posición de la pregunta dentro del cuestionario
     */
    public void agregarPregunta(Pregunta pregunta, int nroOrden) {
        preguntas.add(new PreguntaAsignada(this, pregunta, nroOrden));
    }

    /**
     * Registra las respuestas del bloque y lo marca como completado (CU 026 - paso 7).
     *
     * @param opcionPorPregunta opción elegida para cada pregunta asignada del bloque
     * @param ahora             fecha y hora de la respuesta
     */
    public void responder(Map<PreguntaAsignada, OpcionRespuesta> opcionPorPregunta, LocalDateTime ahora) {
        opcionPorPregunta.forEach((asignada, opcion) -> asignada.responder(opcion, ahora));
        this.completado = Boolean.TRUE;
    }

    public boolean estaCompleto() {
        return Boolean.TRUE.equals(completado);
    }

    public Long getId() {
        return id;
    }

    public Cuestionario getCuestionario() {
        return cuestionario;
    }

    public Integer getNumero() {
        return numero;
    }

    public List<PreguntaAsignada> getPreguntas() {
        return preguntas;
    }
}
