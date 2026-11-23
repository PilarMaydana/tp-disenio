package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.factor.model.Factor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.List;

/**
 * Puntaje obtenido en un factor (observación 3 del CU 026): suma de las ponderaciones de las
 * opciones elegidas para las preguntas del factor, dividida 2.
 */
@Entity
@Table(name = "resultado_factor")
public class ResultadoFactor {

    /** Cantidad de preguntas que se toman por factor (y divisor del total del factor). */
    public static final int PREGUNTAS_POR_FACTOR = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resultado_competencia_id", nullable = false)
    private ResultadoCompetencia resultadoCompetencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factor_id", nullable = false)
    private Factor factor;

    @Column(name = "total_factor", nullable = false)
    private Double totalFactor;

    public ResultadoFactor() {
    }

    public ResultadoFactor(Factor factor) {
        this.factor = factor;
    }

    /**
     * Calcula el total del factor a partir de las respuestas a sus preguntas.
     *
     * @param respuestas respuestas del candidato a las preguntas de este factor
     * @return total del factor
     */
    public Double calcularTotal(List<RespuestaCandidato> respuestas) {
        double suma = respuestas.stream()
                .mapToDouble(r -> r.getOpcionElegida().getPonderacion() == null ? 0.0 : r.getOpcionElegida().getPonderacion())
                .sum();
        this.totalFactor = suma / PREGUNTAS_POR_FACTOR;
        return totalFactor;
    }

    public Long getId() {
        return id;
    }

    public ResultadoCompetencia getResultadoCompetencia() {
        return resultadoCompetencia;
    }

    void setResultadoCompetencia(ResultadoCompetencia resultadoCompetencia) {
        this.resultadoCompetencia = resultadoCompetencia;
    }

    public Factor getFactor() {
        return factor;
    }

    public Double getTotalFactor() {
        return totalFactor;
    }
}
