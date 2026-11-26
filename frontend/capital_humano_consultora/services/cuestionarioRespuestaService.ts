import { apiFetch } from './apiClient';

export interface OpcionCuestionario {
  id: number;
  descripcion: string;
}

export interface PreguntaCuestionario {
  id: number;
  enunciado: string;
  competencia: string;
  opciones: OpcionCuestionario[];
  opcionSeleccionadaId: number | null;
}

export interface PuntajeCompetencia {
  competenciaId: number;
  competencia: string;
  puntaje: number;
}

export interface ResultadoCuestionario {
  estado: string;
  respondidas: number;
  totalPreguntas: number;
  puntajes: PuntajeCompetencia[];
}

export const cuestionarioRespuestaService = {
  obtenerPreguntas(cuestionarioId: number) {
    return apiFetch<PreguntaCuestionario[]>(`/cuestionarios/${cuestionarioId}/preguntas`);
  },

  guardarRespuestas(cuestionarioId: number, respuestas: Array<{ preguntaId: number; opcionId: number }>) {
    return apiFetch<ResultadoCuestionario>(`/cuestionarios/${cuestionarioId}/respuestas`, {
      method: 'POST',
      body: JSON.stringify({ respuestas }),
    });
  },
};
