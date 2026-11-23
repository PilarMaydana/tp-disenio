package ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.Puesto;
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
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una Evaluación (CU 025).
 *
 * <p>Agrupa los cuestionarios generados en una misma operación para evaluar a uno o más
 * candidatos en un puesto. Todos los cuestionarios de una evaluación se basan en el mismo
 * modelo de competencias del puesto.</p>
 */
@Entity
@Table(name = "evaluacion")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "puesto_id", nullable = false)
    private Puesto puesto;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_creacion")
    private Long usuarioCreacion;

    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL)
    private List<Cuestionario> cuestionarios = new ArrayList<>();

    public Evaluacion() {
    }

    public Evaluacion(Puesto puesto, LocalDateTime fechaCreacion, Long usuarioCreacion) {
        this.puesto = puesto;
        this.fechaCreacion = fechaCreacion;
        this.usuarioCreacion = usuarioCreacion;
    }

    /**
     * Agrega un cuestionario a la evaluación manteniendo la relación bidireccional.
     *
     * @param cuestionario cuestionario a agregar
     */
    public void agregarCuestionario(Cuestionario cuestionario) {
        cuestionarios.add(cuestionario);
        cuestionario.setEvaluacion(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Puesto getPuesto() {
        return puesto;
    }

    public void setPuesto(Puesto puesto) {
        this.puesto = puesto;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Long getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(Long usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    public List<Cuestionario> getCuestionarios() {
        return cuestionarios;
    }

    public void setCuestionarios(List<Cuestionario> cuestionarios) {
        this.cuestionarios = cuestionarios;
    }
}
