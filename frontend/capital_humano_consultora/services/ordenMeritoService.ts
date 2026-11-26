import { API_BASE_URL, apiFetch } from './apiClient';

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

  async descargarReporte(puestoId: string, evaluacionIds: string[], formato: 'PDF' | 'WORD') {
    const query = new URLSearchParams({ formato });
    evaluacionIds.forEach((id) => query.append('evaluacionIds', id));
    const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null;
    const response = await fetch(`${API_BASE_URL}/ordenes-merito/puestos/${puestoId}/reporte/descargar?${query.toString()}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    });

    if (!response.ok) {
      const body = await response.json().catch(() => null);
      throw new Error(body?.mensaje || body?.message || body?.detail || 'No se pudo descargar el informe.');
    }

    const blob = await response.blob();
    const disposition = response.headers.get('Content-Disposition') || '';
    const nombreCodificado = disposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1];
    const nombreSimple = disposition.match(/filename="?([^";]+)"?/i)?.[1];
    const nombre = nombreCodificado
      ? decodeURIComponent(nombreCodificado)
      : nombreSimple || `orden-merito.${formato === 'PDF' ? 'pdf' : 'docx'}`;
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombre;
    document.body.appendChild(enlace);
    enlace.click();
    enlace.remove();
    URL.revokeObjectURL(url);
  },
};
