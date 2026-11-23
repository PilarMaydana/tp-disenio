import { apiFetch } from '@/services/apiClient';
import { Instrucciones, PasoWizard, RespuestaBloque, ResumenCuestionario } from '../types';

/**
 * Servicio del CU 026 - Completar cuestionario.
 * El candidato se identifica con el token guardado al ingresar (apiFetch lo envía automáticamente).
 */
export const completarCuestionarioService = {
  /** Mensaje 2: obtenerInstrucciones(). */
  obtenerInstrucciones(): Promise<Instrucciones> {
    return apiFetch<Instrucciones>('/cuestionarios/instrucciones');
  },

  /** Datos del cuestionario abierto del candidato. */
  obtenerResumen(): Promise<ResumenCuestionario> {
    return apiFetch<ResumenCuestionario>('/cuestionarios/actual');
  },

  /** Mensaje 8: iniciarOReanudarCuestionario(nroCandidato). */
  iniciarOReanudar(): Promise<PasoWizard> {
    return apiFetch<PasoWizard>('/cuestionarios/actual/iniciar', { method: 'POST' });
  },

  /** Mensaje 47: responderBloque(cuestionarioId, numeroBloque, respuestas). */
  responderBloque(cuestionarioId: number, numeroBloque: number, respuestas: RespuestaBloque[]): Promise<PasoWizard> {
    return apiFetch<PasoWizard>(`/cuestionarios/${cuestionarioId}/bloques/${numeroBloque}`, {
      method: 'POST',
      body: JSON.stringify({ respuestas }),
    });
  },
};
