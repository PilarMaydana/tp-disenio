package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.competencia.model.Competencia;
import ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model.Cuestionario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Puntaje obtenido por un candidato en una competencia de su cuestionario. */
@Entity(name = "PuntajeCompetenciaCuestionario")
@Table(name = "puntaje_competencia_cuestionario", uniqueConstraints = @UniqueConstraint(
        name = "uk_resultado_cuestionario_competencia", columnNames = {"cuestionario_id", "competencia_id"}))
public class ResultadoCompetencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuestionario_id", nullable = false)
    private Cuestionario cuestionario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    @Column(name = "puntaje", nullable = false)
    private Double puntaje;

    public ResultadoCompetencia() { }

    public ResultadoCompetencia(Cuestionario cuestionario, Competencia competencia, Double puntaje) {
        this.cuestionario = cuestionario;
        this.competencia = competencia;
        this.puntaje = puntaje;
    }

    public Long getId() { return id; }
    public Cuestionario getCuestionario() { return cuestionario; }
    public Competencia getCompetencia() { return competencia; }
    public Double getPuntaje() { return puntaje; }
    public void setPuntaje(Double puntaje) { this.puntaje = puntaje; }
}
