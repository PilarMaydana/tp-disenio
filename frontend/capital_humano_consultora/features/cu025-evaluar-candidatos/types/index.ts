/**
 * Tipos del CU 025 - Evaluar a Candidatos.
 * Reflejan los DTO del backend (`/api/candidatos`, `/api/puestos`, `/api/evaluaciones`).
 */

export type TipoDocumento = 'DNI' | 'LE' | 'LC' | 'PP';

/** Candidato de la grilla de búsqueda (CandidatoDTO). */
export interface CandidatoBusqueda {
  id: number;
  nroCandidato: number;
  apellido: string;
  nombre: string;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
}

/** Puesto para la lista de selección (PuestoBusquedaDTO). */
export interface PuestoOpcion {
  id: number;
  codigo: string;
  nombre: string;
  empresa: string;
}

/** Competencia requerida por el puesto (CaracteristicaDTO). */
export interface CaracteristicaPuesto {
  id: number;
  competenciaId: number;
  nombreCompetencia: string;
  ponderacion: number;
}

/** Puesto con sus competencias y ponderaciones (PuestoDTO). */
export interface PuestoDetalle {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  empresa: string;
  caracteristicas: CaracteristicaPuesto[];
}

/** Resultado de validar las competencias del puesto (ValidacionPuestoDTO). */
export interface ValidacionPuesto {
  puestoCodigo: string;
  evaluable: boolean;
  competenciasNoEvaluables: string[];
}

/** Candidato a evaluar con su clave de ingreso (CandidatoClaveDTO). */
export interface CandidatoClave {
  nroCandidato: number;
  apellido: string;
  nombre: string;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  claveAcceso: string;
}

/** Evaluación guardada (EvaluacionCreadaDTO). */
export interface EvaluacionCreada {
  evaluacionId: number;
  puestoCodigo: string;
  puestoNombre: string;
  empresa: string;
  fechaCreacion: string;
  candidatos: CandidatoClave[];
}

/** Filtros de búsqueda de candidatos (paso 2). */
export interface FiltrosCandidatos {
  apellido?: string;
  nombre?: string;
  nroCandidato?: string;
}

/** Archivo Excel descargado del backend. */
export interface ArchivoExcel {
  nombreArchivo: string;
  contenido: Blob;
}
