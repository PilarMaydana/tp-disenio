package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de un cuestionario completo (CU 026 - paso 8). Lo utilizan los reportes
 * CU 027 (comparativo) y CU 028 (orden de mérito).
 *
 * <p>El puntaje obtenido es el promedio de los puntajes de las competencias evaluadas
 * (escala 0 a 10, comparable con las ponderaciones requeridas por el puesto).</p>
 */
@Entity
@Table(name = "resultado")
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuestionario_id", nullable = false, unique = true)
    private Cuestionario cuestionario;

    @Column(name = "puntaje_obtenido", nullable = false)
    private Double puntajeObtenido;

    @Column(name = "fecha_calculo", nullable = false)
    private LocalDateTime fechaCalculo;

    @OneToMany(mappedBy = "resultado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoCompetencia> resultadosCompetencia = new ArrayList<>();

    public Resultado() {
    }

    public Resultado(LocalDateTime fechaCalculo) {
        this.fechaCalculo = fechaCalculo;
    }

    /**
     * Agrega el resultado de una competencia y recalcula el puntaje obtenido.
     *
     * @param rc resultado de la competencia
     */
    public void agregarResultadoCompetencia(ResultadoCompetencia rc) {
        resultadosCompetencia.add(rc);
        rc.setResultado(this);
        this.puntajeObtenido = resultadosCompetencia.stream()
                .mapToDouble(ResultadoCompetencia::getTotalCompetencia)
                .average()
                .orElse(0.0);
    }

    /**
     * Indica si el candidato alcanzó la ponderación mínima requerida en todas las
     * competencias evaluadas (criterio del orden de mérito del CU 028).
     *
     * @return {@code true} si superó todos los mínimos
     */
    public boolean fueAprobado() {
        return !resultadosCompetencia.isEmpty()
                && resultadosCompetencia.stream().allMatch(ResultadoCompetencia::superaMinimoRequerido);
    }

    public Long getId() {
        return id;
    }

    public Cuestionario getCuestionario() {
        return cuestionario;
    }

    void setCuestionario(Cuestionario cuestionario) {
        this.cuestionario = cuestionario;
    }

    public Double getPuntajeObtenido() {
        return puntajeObtenido;
    }

    public LocalDateTime getFechaCalculo() {
        return fechaCalculo;
    }

    public List<ResultadoCompetencia> getResultadosCompetencia() {
        return resultadosCompetencia;
    }
}
