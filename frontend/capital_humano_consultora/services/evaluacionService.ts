import { Evaluacion } from '@/types/evaluacion';
import { apiFetch } from './apiClient';

export const evaluacionService = {
  async getCuestionarioActivo(cuestionarioId?: number): Promise<Evaluacion> {
    try {
      if (!cuestionarioId) throw new Error('No hay un cuestionario asociado a la sesión');
      const resumen = await apiFetch<{
        nroCuestionario: number; titulo: string; puestoNombre: string; empresaNombre: string;
        estado: Evaluacion['estado']; fechaInicio: string | null; fechaFin: string | null;
        ultimaActualizacion: string | null; totalPreguntas: number; preguntasRespondidas: number;
        puntajes: NonNullable<Evaluacion['puntajes']>;
      }>(`/cuestionarios/${cuestionarioId}/resumen`);
      return {
        id: `cuestionario-${resumen.nroCuestionario}`,
        nroCuestionario: resumen.nroCuestionario,
        titulo: resumen.titulo,
        puestoNombre: resumen.puestoNombre,
        empresaNombre: resumen.empresaNombre,
        estado: resumen.estado,
        fechaInicio: resumen.fechaInicio || 'Sin iniciar',
        fechaFin: resumen.fechaFin || undefined,
        ultimaActualizacion: resumen.ultimaActualizacion || '',
        totalPreguntas: resumen.totalPreguntas,
        preguntasRespondidas: resumen.preguntasRespondidas,
        puntajes: resumen.puntajes,
      };
    } catch {
      // Fallback para desarrollo offline / testing

      // Si el cuestionario es el 2 (TEST4567 - María Gómez), está en proceso (reingreso para continuar)
      if (cuestionarioId === 2) {
        return {
          id: 'cuestionario-2',
          nroCuestionario: 2,
          titulo: 'Cuestionario de Evaluación de Competencias',
          puestoNombre: 'Analista de Selección y RRHH',
          empresaNombre: 'Consultora Capit@l Humano',
          estado: 'in-progress',
          fechaInicio: '18/11/2026 14:00',
          ultimaActualizacion: '18/11/2026 14:45',
          tiempoRestante: '35 minutos restantes',
          totalPreguntas: 20,
          preguntasRespondidas: 8,
        };
      }

      // Por defecto (ej. cuestionario 1 / CLAVE123 - Juan Pérez), está activo listo para comenzar
      return {
        id: 'cuestionario-1',
        nroCuestionario: 1,
        titulo: 'Cuestionario de Evaluación de Competencias',
        puestoNombre: 'Desarrollador Full Stack Senior',
        empresaNombre: 'Consultora Capit@l Humano',
        estado: 'active',
        fechaInicio: 'Sin iniciar',
        ultimaActualizacion: 'Hoy',
        tiempoRestante: 'Tiempo máximo disponible: 60 minutos',
        totalPreguntas: 20,
        preguntasRespondidas: 0,
      };
    }
  },

  // Método legacy por compatibilidad
  async getEvaluacionesCandidato(): Promise<Evaluacion[]> {
    const active = await this.getCuestionarioActivo();
    return [active];
  },
};
