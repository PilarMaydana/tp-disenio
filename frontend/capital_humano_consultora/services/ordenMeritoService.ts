import { apiFetch } from './apiClient';

export interface PuestoOrdenMerito {
  id: string;
  codigo: string;
  nombrePuesto: string;
  empresaId: string;
  empresaNombre: string;
  descripcion: string;
  candidatos: number;
  evaluacionesCompletas: number;
}

export interface EvaluacionOrdenMerito {
  id: string;
  fecha: string;
  fechaISO: string;
  proyecto: string;
  estado: 'Completada' | 'Parcial';
}

export interface CandidatoOrdenMerito {
  cuestionarioId: string;
  tipoDocumento: string;
  nroDocumento: string;
  apellido: string;
  nombre: string;
  estadoCuestionario: string;
  puntajeObtenido: number | null;
  estadoMerito: 'APTO' | 'NO_ALCANZA_MINIMOS' | 'PENDIENTE_PUNTAJE' | 'CUESTIONARIO_INCOMPLETO';
  cumpleMinimos: boolean | null;
  fechaInicio: string | null;
  fechaFinalizacion: string | null;
  datosAdicionales: string | null;
}

interface PuestoOrdenMeritoResponse extends Omit<PuestoOrdenMerito, 'id' | 'empresaId'> {
  id: number;
  empresaId: string;
}

interface EvaluacionOrdenMeritoResponse extends Omit<EvaluacionOrdenMerito, 'id'> { id: number }
interface CandidatoOrdenMeritoResponse extends Omit<CandidatoOrdenMerito, 'cuestionarioId'> { cuestionarioId: number }

export const ordenMeritoService = {
  async buscarPuestos(filtros: { codigo?: string; nombrePuesto?: string; empresa?: string }) {
    const data = await apiFetch<PuestoOrdenMeritoResponse[]>('/ordenes-merito/puestos', { params: filtros });
    return data.map((puesto) => ({ ...puesto, id: String(puesto.id), empresaId: puesto.empresaNombre }));
  },

  async listarEmpresas() {
    const nombres = await apiFetch<string[]>('/ordenes-merito/empresas');
    return nombres.map((nombre) => ({ id: nombre, nombre }));
  },

  async listarEvaluaciones(puestoId: string) {
    const data = await apiFetch<EvaluacionOrdenMeritoResponse[]>(`/ordenes-merito/puestos/${puestoId}/evaluaciones`);
    return data.map((evaluacion) => ({ ...evaluacion, id: String(evaluacion.id) }));
  },

  async obtenerReporte(puestoId: string, evaluacionIds: string[]) {
    const query = new URLSearchParams();
    evaluacionIds.forEach((id) => query.append('evaluacionIds', id));
    const data = await apiFetch<CandidatoOrdenMeritoResponse[]>(`/ordenes-merito/puestos/${puestoId}/reporte?${query.toString()}`);
    return data.map((candidato) => ({ ...candidato, cuestionarioId: String(candidato.cuestionarioId) }));
  },
};
