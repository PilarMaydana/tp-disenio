/**
 * Tipos del CU 026 - Completar cuestionario.
 * Reflejan los DTO del backend (`/api/cuestionarios`).
 */

export type EstadoCuestionario = 'ACTIVO' | 'EN_PROCESO' | 'COMPLETO' | 'INCOMPLETO' | 'SIN_CONTESTAR';

/** Pantalla "Cuestionario - Instrucciones" (InstruccionesDTO). */
export interface Instrucciones {
  instrucciones: string;
  tiempoMaximoMinutos: number;
  cantidadPreguntasPorBloque: number;
  cantidadMaximaAccesos: number;
}

/** Cuestionario abierto del candidato (CuestionarioResumenDTO). */
export interface ResumenCuestionario {
  cuestionarioId: number;
  estado: EstadoCuestionario;
  puestoNombre: string;
  empresa: string;
  fechaCreacion: string | null;
  fechaInicio: string | null;
  fechaUltimoIngreso: string | null;
  cantidadAccesos: number;
  cantidadMaximaAccesos: number | null;
  tiempoMaximoMinutos: number | null;
  totalPreguntas: number;
  preguntasRespondidas: number;
  segundosRestantes: number | null;
}

export interface OpcionCuestionario {
  id: number;
  descripcion: string;
}

export interface PreguntaCuestionario {
  preguntaAsignadaId: number;
  nroOrden: number;
  enunciado: string;
  opciones: OpcionCuestionario[];
}

/** Paso del wizard (CuestionarioWizardDTO). */
export interface PasoWizard {
  cuestionarioId: number;
  puestoNombre: string;
  empresa: string;
  estado: EstadoCuestionario;
  numeroBloque: number | null;
  totalBloques: number;
  totalPreguntas: number;
  preguntasRespondidas: number;
  segundosRestantes: number | null;
  completado: boolean;
  preguntas: PreguntaCuestionario[];
}

export interface RespuestaBloque {
  preguntaAsignadaId: number;
  opcionId: number;
}
