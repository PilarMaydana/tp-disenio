'use client';

import React, { useEffect, useState } from 'react';
import { AlertCircle, CheckCircle2, X } from 'lucide-react';
import { evaluacionService } from '@/services/evaluacionService';
import { Evaluacion } from '@/types/evaluacion';
import { cuestionarioRespuestaService, PreguntaCuestionario, ResultadoCuestionario } from '@/services/cuestionarioRespuestaService';
import { EvaluationCard } from '@/components/organisms/EvaluationCard';
import { Button } from '@/components/ui/Button';
import { useAuth } from '@/context/AuthContext';
import { useAutoDismissError } from '@/hooks/useAutoDismissError';

export default function MiCuestionarioPage() {
  const { user } = useAuth();
  const [evaluacion, setEvaluacion] = useState<Evaluacion | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [preguntas, setPreguntas] = useState<PreguntaCuestionario[]>([]);
  const [selecciones, setSelecciones] = useState<Record<number, number>>({});
  const [iniciado, setIniciado] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState('');
  const mostrarError = useAutoDismissError(error, () => setError(''));
  const [resultado, setResultado] = useState<ResultadoCuestionario | null>(null);

  useEffect(() => {
    // Se obtiene el cuestionario activo asignado a la clave con la que ingresó el candidato
    evaluacionService.getCuestionarioActivo(user?.cuestionarioId).then((data) => {
      setEvaluacion(data);
      setIsLoading(false);
    });
  }, [user?.cuestionarioId]);

  const handleAction = async (_item: Evaluacion) => {
    if (!user?.cuestionarioId) {
      setError('No se encontró el cuestionario asociado a esta sesión. Volvé a iniciar sesión.');
      return;
    }
    setError('');
    setGuardando(true);
    try {
      if (_item.estado === 'finished') {
        const resumen = await evaluacionService.getCuestionarioActivo(user.cuestionarioId);
        setResultado({ estado: 'COMPLETO', respondidas: resumen.preguntasRespondidas || 0,
          totalPreguntas: resumen.totalPreguntas || 0, puntajes: resumen.puntajes || [] });
        return;
      }
      const data = await cuestionarioRespuestaService.obtenerPreguntas(user.cuestionarioId);
      setPreguntas(data);
      setSelecciones(Object.fromEntries(data.filter((p) => p.opcionSeleccionadaId != null)
        .map((p) => [p.id, p.opcionSeleccionadaId as number])));
      setIniciado(true);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo cargar el cuestionario.');
    } finally {
      setGuardando(false);
    }
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!user?.cuestionarioId) return;
    if (Object.keys(selecciones).length !== preguntas.length) {
      setError('Respondé todas las preguntas antes de finalizar.');
      return;
    }
    setError('');
    setGuardando(true);
    try {
      const respuesta = await cuestionarioRespuestaService.guardarRespuestas(user.cuestionarioId,
        preguntas.map((pregunta) => ({ preguntaId: pregunta.id, opcionId: selecciones[pregunta.id] })));
      setResultado(respuesta);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudieron guardar las respuestas.');
    } finally {
      setGuardando(false);
    }
  };

  const handleSaveProgress = async () => {
    if (!user?.cuestionarioId || Object.keys(selecciones).length === 0) {
      setError('Seleccioná al menos una respuesta para guardar el avance.');
      return;
    }
    setError('');
    setGuardando(true);
    try {
      const guardado = await cuestionarioRespuestaService.guardarRespuestas(user.cuestionarioId,
        Object.entries(selecciones).map(([preguntaId, opcionId]) => ({ preguntaId: Number(preguntaId), opcionId })));
      setEvaluacion((actual) => actual ? { ...actual, estado: 'in-progress', preguntasRespondidas: guardado.respondidas } : actual);
      setIniciado(false);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo guardar el avance.');
    } finally {
      setGuardando(false);
    }
  };

  return (
    <div className="flex flex-col gap-6 w-full max-w-5xl mx-auto">
      {/* Título de la pantalla */}
      <div className="flex flex-col">
        <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
          {evaluacion?.estado === 'in-progress'
            ? 'Cuestionario en Proceso'
            : 'Cuestionario Asignado'}
        </h1>
        <p className="text-sm md:text-base text-[#555555] mt-1 font-medium">
          {resultado ? 'Tus respuestas fueron registradas y el puntaje fue calculado.' : evaluacion?.estado === 'in-progress'
            ? 'Presione "Continuar" para reanudar la evaluación.'
            : 'Presione "Comenzar" para iniciar la evaluación.'}
        </p>
      </div>

      {/* Cuestionario Único Activo / En Proceso */}
      {isLoading ? (
        <div className="p-12 text-center text-[#757575]">Cargando cuestionario...</div>
      ) : resultado ? (
        <section className="rounded-xl border border-[#B9E4C7] bg-white p-6 shadow-sm">
          <div className="flex items-center gap-3"><CheckCircle2 className="text-[#1B8354]" size={28}/><div><h2 className="text-xl font-semibold text-[#1B8354]">Cuestionario completado</h2><p className="text-sm text-[#555]">{resultado.respondidas} de {resultado.totalPreguntas} respuestas guardadas.</p></div></div>
          <div className="mt-5 grid gap-3 sm:grid-cols-2">{resultado.puntajes.map((puntaje) => <div key={puntaje.competenciaId} className="rounded-lg bg-[#F4F0FF] p-4"><p className="text-sm text-[#625D67]">{puntaje.competencia}</p><p className="mt-1 text-2xl font-bold text-[#3F2581]">{puntaje.puntaje.toFixed(2)} <span className="text-sm font-medium">/ 10</span></p></div>)}</div>
        </section>
      ) : iniciado ? (
        <form onSubmit={handleSubmit} className="space-y-5">
          {preguntas.map((pregunta, index) => <fieldset key={pregunta.id} className="rounded-xl border border-[#D0BCFF] bg-white p-5 shadow-sm">
            <legend className="px-1 text-xs font-semibold uppercase tracking-wide text-[#63499A]">{pregunta.competencia}</legend>
            <p className="mb-4 font-semibold text-[#29252E]">{index + 1}. {pregunta.enunciado}</p>
            <div className="space-y-2">{pregunta.opciones.map((opcion) => <label key={opcion.id} className="flex cursor-pointer items-start gap-3 rounded-lg border border-[#E7E2EF] p-3 text-sm hover:bg-[#F8F5FF]">
              <input type="radio" name={`pregunta-${pregunta.id}`} value={opcion.id} checked={selecciones[pregunta.id] === opcion.id} onChange={() => setSelecciones((prev) => ({ ...prev, [pregunta.id]: opcion.id }))} className="mt-0.5 accent-[#3F2581]"/>
              <span>{opcion.descripcion}</span>
            </label>)}</div>
          </fieldset>)}
          {mostrarError && error && <div role="alert" className="flex items-start gap-3 rounded-lg bg-red-50 p-3 text-sm text-red-700"><AlertCircle size={18} className="mt-0.5 shrink-0" aria-hidden="true"/><p className="flex-1">{error}</p><button type="button" onClick={() => setError('')} aria-label="Cerrar mensaje de error" title="Cerrar mensaje de error" className="rounded p-1 hover:bg-red-100"><X size={16} aria-hidden="true"/></button></div>}
          <div className="flex flex-wrap justify-end gap-3"><Button type="button" variant="secondary" onClick={handleSaveProgress} isLoading={guardando} disabled={preguntas.length === 0 || Object.keys(selecciones).length === 0 || Object.keys(selecciones).length === preguntas.length}>Guardar avance y salir</Button><Button type="submit" isLoading={guardando} disabled={preguntas.length === 0 || guardando}>Finalizar cuestionario</Button></div>
        </form>
      ) : evaluacion ? (
        <div className="flex flex-col gap-4">
          <EvaluationCard evaluacion={evaluacion} onAction={handleAction} />
          {mostrarError && error && <div role="alert" className="flex items-start gap-3 rounded-lg bg-red-50 p-3 text-sm text-red-700"><AlertCircle size={18} className="mt-0.5 shrink-0" aria-hidden="true"/><p className="flex-1">{error}</p><button type="button" onClick={() => setError('')} aria-label="Cerrar mensaje de error" title="Cerrar mensaje de error" className="rounded p-1 hover:bg-red-100"><X size={16} aria-hidden="true"/></button></div>}
          {guardando && <p className="text-center text-sm text-[#757575]">Cargando cuestionario…</p>}
        </div>
      ) : (
        <div className="p-8 text-center text-[#757575] bg-white rounded-xl border border-gray-200">
          No posee un cuestionario activo en este momento.
        </div>
      )}
    </div>
  );
}
