import { apiFetch, ApiError, API_BASE_URL } from '@/services/apiClient';
import {
  ArchivoExcel,
  CandidatoBusqueda,
  CandidatoClave,
  EvaluacionCreada,
  FiltrosCandidatos,
  PuestoDetalle,
  PuestoOpcion,
  ValidacionPuesto,
} from '../types';

/**
 * Servicio del CU 025 - Evaluar a Candidatos.
 * Cada método corresponde a un mensaje del diagrama de secuencia.
 * No silencia errores HTTP: los flujos alternativos (4.A, 6.A, 9.A, 10.A) llegan como ApiError 422.
 */
export const evaluarCandidatosService = {
  /** Mensaje 4: buscarCandidatos(apellido, nombre, nroCandidato). */
  buscarCandidatos(filtros: FiltrosCandidatos = {}): Promise<CandidatoBusqueda[]> {
    return apiFetch<CandidatoBusqueda[]>('/candidatos', {
      params: {
        apellido: filtros.apellido?.trim(),
        nombre: filtros.nombre?.trim(),
        nroCandidato: filtros.nroCandidato?.trim(),
      },
    });
  },

  /** Mensaje 17: verificarSinCuestionarioActivo para cada candidato seleccionado. */
  validarCandidatos(nroCandidatos: number[]): Promise<CandidatoBusqueda[]> {
    return apiFetch<CandidatoBusqueda[]>('/evaluaciones/validar-candidatos', {
      method: 'POST',
      body: JSON.stringify({ nroCandidatos }),
    });
  },

  /** Mensaje 23: buscar(codigo=null, nombre=null, empresa=null). */
  listarPuestos(): Promise<PuestoOpcion[]> {
    return apiFetch<PuestoOpcion[]>('/puestos');
  },

  /** Mensaje 29: obtener(puestoCodigo). */
  obtenerPuesto(codigo: string): Promise<PuestoDetalle> {
    return apiFetch<PuestoDetalle>(`/puestos/${encodeURIComponent(codigo)}`);
  },

  /** Mensaje 35: validarCompetenciasEvaluables(puestoCodigo). */
  validarPuesto(codigo: string): Promise<ValidacionPuesto> {
    return apiFetch<ValidacionPuesto>(`/evaluaciones/puestos/${encodeURIComponent(codigo)}/validacion`);
  },

  /** Paso 11: genera las claves de ingreso que se muestran antes de Finalizar (no guarda). */
  prepararEvaluacion(nroCandidatos: number[], puestoCodigo: string): Promise<CandidatoClave[]> {
    return apiFetch<CandidatoClave[]>('/evaluaciones/preparar', {
      method: 'POST',
      body: JSON.stringify({ nroCandidatos, puestoCodigo }),
    });
  },

  /** Mensaje 51: evaluarCandidatos(nroCandidatos, puestoCodigo) con las claves mostradas. */
  evaluarCandidatos(puestoCodigo: string, candidatos: CandidatoClave[]): Promise<EvaluacionCreada> {
    return apiFetch<EvaluacionCreada>('/evaluaciones', {
      method: 'POST',
      body: JSON.stringify({ puestoCodigo, candidatos }),
    });
  },

  /** Mensaje 70: exportarResultados(evaluación) - descarga el .xls generado por el backend. */
  async exportarExcel(evaluacionId: number, nombrePorDefecto: string): Promise<ArchivoExcel> {
    const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null;
    const response = await fetch(`${API_BASE_URL}/evaluaciones/${evaluacionId}/excel`, {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    });
    if (!response.ok) {
      const data = await response.json().catch(() => null);
      throw new ApiError(response.status, data?.mensaje || 'No se pudo generar el archivo Excel', data);
    }
    return {
      nombreArchivo: nombreDesdeContentDisposition(response.headers.get('Content-Disposition')) ?? nombrePorDefecto,
      contenido: await response.blob(),
    };
  },
};

/** Extrae el nombre del archivo del header Content-Disposition (soporta filename*=UTF-8''...). */
function nombreDesdeContentDisposition(header: string | null): string | null {
  if (!header) return null;
  const utf8 = /filename\*=UTF-8''([^;]+)/i.exec(header);
  if (utf8) {
    try {
      return decodeURIComponent(utf8[1]);
    } catch {
      /* se usa el filename simple */
    }
  }
  const simple = /filename="?([^";]+)"?/i.exec(header);
  return simple ? simple[1] : null;
}
