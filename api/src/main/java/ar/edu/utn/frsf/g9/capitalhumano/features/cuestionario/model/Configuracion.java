package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Parámetros generales de los cuestionarios: instrucciones, tiempos y tamaño de los bloques
 * (CU 026 - "las instrucciones y los parámetros se toman de un repositorio de configuración").
 */
@Entity
@Table(name = "configuracion")
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrucciones", length = 2000, nullable = false)
    private String instrucciones;

    /** Tiempo máximo para completar el cuestionario, desde el primer ingreso (1.A). */
    @Column(name = "tiempo_maximo_minutos", nullable = false)
    private Integer tiempoMaximoMinutos;

    /** Tiempo máximo que el cuestionario puede permanecer ACTIVO sin ser iniciado (1.B). */
    @Column(name = "tiempo_maximo_activo_horas", nullable = false)
    private Integer tiempoMaximoActivoHoras;

    /** Cantidad máxima de ingresos permitidos al candidato (precondición). */
    @Column(name = "cantidad_maxima_accesos", nullable = false)
    private Integer cantidadMaximaAccesos;

    /** Cantidad de preguntas por paso del wizard. */
    @Column(name = "cantidad_preguntas_por_bloque", nullable = false)
    private Integer cantidadPreguntasPorBloque;

    public Configuracion() {
    }

    public Configuracion(String instrucciones, Integer tiempoMaximoMinutos, Integer tiempoMaximoActivoHoras,
                         Integer cantidadMaximaAccesos, Integer cantidadPreguntasPorBloque) {
        this.instrucciones = instrucciones;
        this.tiempoMaximoMinutos = tiempoMaximoMinutos;
        this.tiempoMaximoActivoHoras = tiempoMaximoActivoHoras;
        this.cantidadMaximaAccesos = cantidadMaximaAccesos;
        this.cantidadPreguntasPorBloque = cantidadPreguntasPorBloque;
    }

    public Long getId() {
        return id;
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public Integer getTiempoMaximoMinutos() {
        return tiempoMaximoMinutos;
    }

    public Integer getTiempoMaximoActivoHoras() {
        return tiempoMaximoActivoHoras;
    }

    public Integer getCantidadMaximaAccesos() {
        return cantidadMaximaAccesos;
    }

    public Integer getCantidadPreguntasPorBloque() {
        return cantidadPreguntasPorBloque;
    }
}
