package ar.edu.utn.frsf.g9.capitalhumano.features.cuestionario.model;

import ar.edu.utn.frsf.g9.capitalhumano.features.candidato.model.Candidato;
import ar.edu.utn.frsf.g9.capitalhumano.features.evaluacion.model.Evaluacion;
import ar.edu.utn.frsf.g9.capitalhumano.shared.model.EntidadAuditable;
import ar.edu.utn.frsf.g9.capitalhumano.features.pregunta.model.Pregunta;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Entidad JPA que representa un Cuestionario asignado a un candidato para un proceso de evaluación.
 */
@Entity
@Table(name = "cuestionario")
public class Cuestionario extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidato_id", nullable = false)
    private Candidato candidato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluacion_id")
    private Evaluacion evaluacion;

    @Column(name = "puesto_id")
    private Long puestoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoCuestionario estado;

    @Column(name = "clave_acceso", length = 8, nullable = false)
    private String claveAcceso;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    // ---------------------------------------------------------------- CU 026
    @Column(name = "fecha_ultimo_ingreso")
    private LocalDateTime fechaUltimoIngreso;

    @Column(name = "fecha_cambio_estado")
    private LocalDateTime fechaCambioEstado;

    @Column(name = "cantidad_accesos", nullable = false)
    private Integer cantidadAccesos = 0;

    @Column(name = "tiempo_maximo_minutos")
    private Integer tiempoMaximoMinutos;

    @Column(name = "tiempo_maximo_activo_horas")
    private Integer tiempoMaximoActivoHoras;

    @Column(name = "cantidad_maxima_accesos")
    private Integer cantidadMaximaAccesos;

    @Column(name = "cantidad_preguntas_por_bloque")
    private Integer cantidadPreguntasPorBloque;

    @OneToMany(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero ASC")
    private List<BloqueCuestionario> bloques = new ArrayList<>();

    @OneToOne(mappedBy = "cuestionario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Resultado resultado;

    public Cuestionario() {
    }

    public Cuestionario(Long id, Candidato candidato, EstadoCuestionario estado, String claveAcceso) {
        this.id = id;
        this.candidato = candidato;
        this.estado = estado;
        this.claveAcceso = claveAcceso;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Candidato getCandidato() {
        return candidato;
    }

    public void setCandidato(Candidato candidato) {
        this.candidato = candidato;
    }

    public Long getPuestoId() {
        return puestoId;
    }

    public void setPuestoId(Long puestoId) {
        this.puestoId = puestoId;
    }

    public EstadoCuestionario getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuestionario estado) {
        this.estado = estado;
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public Evaluacion getEvaluacion() {
        return evaluacion;
    }

    public void setEvaluacion(Evaluacion evaluacion) {
        this.evaluacion = evaluacion;
    }

    /**
     * Indica si el cuestionario está ACTIVO o EN_PROCESO, es decir, si impide
     * que el candidato sea evaluado nuevamente (CU 025 - flujo 6.A).
     *
     * @return {@code true} si el cuestionario está activo o en proceso
     */
    public boolean estaActivo() {
        return estado == EstadoCuestionario.ACTIVO || estado == EstadoCuestionario.EN_PROCESO;
    }

    // ================================================================ CU 026

    /**
     * Copia en el cuestionario los parámetros vigentes de la configuración, para que
     * un cambio posterior de la configuración no afecte a los cuestionarios ya creados.
     *
     * @param configuracion parámetros generales del sistema
     */
    public void aplicarConfiguracion(Configuracion configuracion) {
        this.tiempoMaximoMinutos = configuracion.getTiempoMaximoMinutos();
        this.tiempoMaximoActivoHoras = configuracion.getTiempoMaximoActivoHoras();
        this.cantidadMaximaAccesos = configuracion.getCantidadMaximaAccesos();
        this.cantidadPreguntasPorBloque = configuracion.getCantidadPreguntasPorBloque();
    }

    /**
     * 1.A - Indica si, estando en proceso, se superó el tiempo máximo para completarlo.
     * Se mide desde la fecha y hora del primer ingreso (observación 2 del CU 026).
     *
     * @param ahora fecha y hora actual
     * @return {@code true} si se superó el tiempo máximo
     */
    public boolean superaTiempoMaximo(LocalDateTime ahora) {
        if (fechaInicio == null || tiempoMaximoMinutos == null) {
            return false;
        }
        return Duration.between(fechaInicio, ahora).getSeconds() > tiempoMaximoMinutos * 60L;
    }

    /**
     * 1.B - Indica si el cuestionario permaneció en estado ACTIVO más tiempo del permitido.
     *
     * @param ahora fecha y hora actual
     * @return {@code true} si se superó el tiempo máximo en estado activo
     */
    public boolean superaTiempoMaximoActivo(LocalDateTime ahora) {
        if (fechaCreacion == null || tiempoMaximoActivoHoras == null) {
            return false;
        }
        return Duration.between(fechaCreacion, ahora).getSeconds() > tiempoMaximoActivoHoras * 3600L;
    }

    /**
     * Precondición del CU 026 - Indica si ya se usaron todos los accesos permitidos.
     *
     * @return {@code true} si no quedan accesos disponibles
     */
    public boolean superaMaximoAccesos() {
        return cantidadMaximaAccesos != null && cantidadAccesos != null
                && cantidadAccesos >= cantidadMaximaAccesos;
    }

    /**
     * Registra un nuevo ingreso del candidato al cuestionario.
     *
     * @param ahora fecha y hora del ingreso
     */
    public void registrarAcceso(LocalDateTime ahora) {
        this.cantidadAccesos = (cantidadAccesos == null ? 0 : cantidadAccesos) + 1;
        this.fechaUltimoIngreso = ahora;
    }

    /**
     * Cambia el estado del cuestionario registrando la fecha y hora del cambio.
     *
     * @param nuevoEstado estado nuevo
     * @param ahora       fecha y hora del cambio
     */
    public void cambiarEstado(EstadoCuestionario nuevoEstado, LocalDateTime ahora) {
        this.estado = nuevoEstado;
        this.fechaCambioEstado = ahora;
    }

    /**
     * Segundos que le quedan al candidato para completar el cuestionario.
     *
     * @param ahora fecha y hora actual
     * @return segundos restantes (0 si ya se agotó), o {@code null} si todavía no comenzó
     */
    public Long segundosRestantes(LocalDateTime ahora) {
        if (fechaInicio == null || tiempoMaximoMinutos == null) {
            return null;
        }
        long restantes = Duration.between(ahora, fechaInicio.plusMinutes(tiempoMaximoMinutos)).getSeconds();
        return Math.max(restantes, 0);
    }

    /**
     * Primer bloque que todavía no fue respondido (paso 4 del CU 026).
     *
     * @return bloque pendiente, o {@code null} si están todos respondidos
     */
    public BloqueCuestionario getBloquePendiente() {
        return bloques.stream()
                .filter(b -> !b.estaCompleto())
                .min(Comparator.comparing(BloqueCuestionario::getNumero))
                .orElse(null);
    }

    /**
     * Distribuye las preguntas (ya mezcladas al azar) en bloques según la cantidad de
     * preguntas por bloque y los agrega al cuestionario (paso 3 del CU 026).
     *
     * @param preguntas preguntas seleccionadas, en el orden en que se mostrarán
     * @return bloques generados
     */
    public List<BloqueCuestionario> armarBloques(List<Pregunta> preguntas) {
        int porBloque = cantidadPreguntasPorBloque != null && cantidadPreguntasPorBloque > 0
                ? cantidadPreguntasPorBloque
                : Math.max(preguntas.size(), 1);
        bloques.clear();
        BloqueCuestionario bloque = null;
        for (int i = 0; i < preguntas.size(); i++) {
            if (i % porBloque == 0) {
                bloque = new BloqueCuestionario(this, (i / porBloque) + 1);
                bloques.add(bloque);
            }
            bloque.agregarPregunta(preguntas.get(i), i + 1);
        }
        return bloques;
    }

    /**
     * Cantidad total de preguntas asignadas al cuestionario.
     *
     * @return total de preguntas
     */
    public int getCantidadPreguntas() {
        return bloques.stream().mapToInt(b -> b.getPreguntas().size()).sum();
    }

    /**
     * Cantidad de preguntas ya respondidas.
     *
     * @return preguntas con respuesta
     */
    public int getCantidadRespondidas() {
        return (int) bloques.stream()
                .flatMap(b -> b.getPreguntas().stream())
                .filter(PreguntaAsignada::fueRespondida)
                .count();
    }

    public LocalDateTime getFechaUltimoIngreso() {
        return fechaUltimoIngreso;
    }

    public void setFechaUltimoIngreso(LocalDateTime fechaUltimoIngreso) {
        this.fechaUltimoIngreso = fechaUltimoIngreso;
    }

    public LocalDateTime getFechaCambioEstado() {
        return fechaCambioEstado;
    }

    public void setFechaCambioEstado(LocalDateTime fechaCambioEstado) {
        this.fechaCambioEstado = fechaCambioEstado;
    }

    public Integer getCantidadAccesos() {
        return cantidadAccesos;
    }

    public void setCantidadAccesos(Integer cantidadAccesos) {
        this.cantidadAccesos = cantidadAccesos;
    }

    public Integer getTiempoMaximoMinutos() {
        return tiempoMaximoMinutos;
    }

    public void setTiempoMaximoMinutos(Integer tiempoMaximoMinutos) {
        this.tiempoMaximoMinutos = tiempoMaximoMinutos;
    }

    public Integer getTiempoMaximoActivoHoras() {
        return tiempoMaximoActivoHoras;
    }

    public void setTiempoMaximoActivoHoras(Integer tiempoMaximoActivoHoras) {
        this.tiempoMaximoActivoHoras = tiempoMaximoActivoHoras;
    }

    public Integer getCantidadMaximaAccesos() {
        return cantidadMaximaAccesos;
    }

    public void setCantidadMaximaAccesos(Integer cantidadMaximaAccesos) {
        this.cantidadMaximaAccesos = cantidadMaximaAccesos;
    }

    public Integer getCantidadPreguntasPorBloque() {
        return cantidadPreguntasPorBloque;
    }

    public void setCantidadPreguntasPorBloque(Integer cantidadPreguntasPorBloque) {
        this.cantidadPreguntasPorBloque = cantidadPreguntasPorBloque;
    }

    public List<BloqueCuestionario> getBloques() {
        return bloques;
    }

    public void setBloques(List<BloqueCuestionario> bloques) {
        this.bloques = bloques;
    }

    public Resultado getResultado() {
        return resultado;
    }

    /**
     * Asocia el resultado calculado manteniendo la relación bidireccional.
     *
     * @param resultado resultado del cuestionario
     */
    public void setResultado(Resultado resultado) {
        this.resultado = resultado;
        if (resultado != null) {
            resultado.setCuestionario(this);
        }
    }
}
