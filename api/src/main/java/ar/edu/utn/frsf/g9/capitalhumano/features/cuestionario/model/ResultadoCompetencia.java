package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.puesto.model.CaracteristicaPuesto;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Puntaje obtenido en una competencia del puesto (observación 3 del CU 026): promedio de los
 * totales de los factores evaluados de la competencia.
 */
@Entity
@Table(name = "resultado_competencia")
public class ResultadoCompetencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resultado_id", nullable = false)
    private Resultado resultado;

    /** Característica del puesto (competencia + ponderación requerida) según la cual se evalúa. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caracteristica_puesto_id", nullable = false)
    private CaracteristicaPuesto caracteristicaPuesto;

    @Column(name = "total_competencia", nullable = false)
    private Double totalCompetencia;

    @OneToMany(mappedBy = "resultadoCompetencia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoFactor> resultadosFactor = new ArrayList<>();

    public ResultadoCompetencia() {
    }

    public ResultadoCompetencia(CaracteristicaPuesto caracteristicaPuesto) {
        this.caracteristicaPuesto = caracteristicaPuesto;
    }

    /**
     * Agrega el resultado de un factor de la competencia.
     *
     * @param rf resultado del factor
     */
    public void agregarResultadoFactor(ResultadoFactor rf) {
        resultadosFactor.add(rf);
        rf.setResultadoCompetencia(this);
    }

    /**
     * Calcula el total de la competencia: suma de los totales de sus factores dividida por la
     * cantidad de factores evaluados.
     *
     * @return total de la competencia
     */
    public Double calcularTotal() {
        this.totalCompetencia = resultadosFactor.stream()
                .mapToDouble(ResultadoFactor::getTotalFactor)
                .average()
                .orElse(0.0);
        return totalCompetencia;
    }

    /**
     * Indica si el total alcanza la ponderación mínima requerida por el puesto.
     *
     * @return {@code true} si alcanza el mínimo
     */
    public boolean superaMinimoRequerido() {
        Double requerida = caracteristicaPuesto.getPonderacionRequerida();
        return totalCompetencia != null && requerida != null && totalCompetencia >= requerida;
    }

    public Long getId() {
        return id;
    }

    public Resultado getResultado() {
        return resultado;
    }

    void setResultado(Resultado resultado) {
        this.resultado = resultado;
    }

    public CaracteristicaPuesto getCaracteristicaPuesto() {
        return caracteristicaPuesto;
    }

    public Double getTotalCompetencia() {
        return totalCompetencia;
    }

    public List<ResultadoFactor> getResultadosFactor() {
        return resultadosFactor;
    }
}
