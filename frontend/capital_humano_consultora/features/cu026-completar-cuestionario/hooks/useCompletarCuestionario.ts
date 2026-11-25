'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError } from '@/services/apiClient';
import { completarCuestionarioService } from '../services/completarCuestionarioService';
import { emitirAlertaSonora } from '../services/alertaSonora';
import { Instrucciones, PasoWizard, ResumenCuestionario } from '../types';

/**
 * Fases de la pantalla:
 * - instrucciones: pantalla "Cuestionario - Instrucciones" (pasos 1 y 2)
 * - cancelado: el actor presionó "Cancelar" (2.A), el cuestionario no cambia
 * - wizard: bloque de preguntas a responder (pasos 4 a 7)
 * - completado: el cuestionario se completó (paso 8)
 * - no-disponible: se superó el tiempo o los accesos (1.A / 1.B)
 * - sin-cuestionario: el candidato no tiene un cuestionario abierto
 */
export type FaseCuestionario =
  | 'cargando'
  | 'instrucciones'
  | 'cancelado'
  | 'wizard'
  | 'completado'
  | 'no-disponible'
  | 'sin-cuestionario'
  | 'error';

const codigoDe = (error: unknown): string | undefined =>
  error instanceof ApiError ? (error.data as { codigo?: string } | undefined)?.codigo : undefined;

const mensajeDe = (error: unknown): string =>
  error instanceof Error ? error.message : 'Ocurrió un error inesperado';

/**
 * Estado y lógica de la pantalla del CU 026 - Completar cuestionario.
 */
export function useCompletarCuestionario() {
  const [fase, setFase] = useState<FaseCuestionario>('cargando');
  const [enviando, setEnviando] = useState(false);
  /**
   * Pedido en curso («Aceptar» o «Siguiente»/«Finalizar»). El estado "enviando" recién se
   * aplica en el próximo render, por eso se usa una referencia que se actualiza en el acto
   * y evita que un doble click envíe dos pedidos (y descuente dos accesos).
   */
  const enCursoRef = useRef(false);
  const [mensaje, setMensaje] = useState<string | null>(null);

  const [instrucciones, setInstrucciones] = useState<Instrucciones | null>(null);
  const [resumen, setResumen] = useState<ResumenCuestionario | null>(null);
  const [paso, setPaso] = useState<PasoWizard | null>(null);

  /** Opción elegida por pregunta del bloque actual (se conserva ante el error 6.A). */
  const [respuestas, setRespuestas] = useState<Record<number, number>>({});
  /** Preguntas sin responder marcadas al validar (6.A). */
  const [faltantes, setFaltantes] = useState<number[]>([]);

  /** Segundos restantes para completar el cuestionario. */
  const [segundos, setSegundos] = useState<number | null>(null);

  // ------------------------------------------------------------ Pasos 1 a 4: ingreso
  useEffect(() => {
    let activo = true;
    Promise.all([completarCuestionarioService.obtenerResumen(), completarCuestionarioService.obtenerInstrucciones()])
      .then(([r, i]) => {
        if (!activo) return;
        setResumen(r);
        setInstrucciones(i);
        setFase('instrucciones');
      })
      .catch((error) => {
        if (!activo) return;
        if (error instanceof ApiError && error.status === 404) {
          setFase('sin-cuestionario');
        } else {
          setMensaje(mensajeDe(error));
          setFase('error');
        }
      });
    return () => {
      activo = false;
    };
  }, []);

  // Cuenta regresiva del tiempo máximo
  useEffect(() => {
    if (fase !== 'wizard') return;
    const intervalo = setInterval(() => {
      setSegundos((s) => (s === null ? s : Math.max(s - 1, 0)));
    }, 1000);
    return () => clearInterval(intervalo);
  }, [fase]);

  const mostrarPaso = useCallback((nuevo: PasoWizard) => {
    setPaso(nuevo);
    setRespuestas({});
    setFaltantes([]);
    setMensaje(null);
    setSegundos(nuevo.segundosRestantes);
    setFase(nuevo.completado ? 'completado' : 'wizard');
  }, []);

  const manejarError = useCallback((error: unknown) => {
    const codigo = codigoDe(error);
    if (codigo === 'CUESTIONARIO_INCOMPLETO' || codigo === 'CUESTIONARIO_SIN_CONTESTAR') {
      // 1.A / 1.B: el cuestionario venció, el CU finaliza
      emitirAlertaSonora();
      setMensaje(mensajeDe(error));
      setFase('no-disponible');
      return;
    }
    emitirAlertaSonora();
    setMensaje(mensajeDe(error));
  }, []);

  // ------------------------------------------------------------ Paso 2: Aceptar / Cancelar
  const aceptar = useCallback(async () => {
    if (enCursoRef.current) return;
    enCursoRef.current = true;
    setEnviando(true);
    setMensaje(null);
    try {
      mostrarPaso(await completarCuestionarioService.iniciarOReanudar());
    } catch (error) {
      manejarError(error);
    } finally {
      enCursoRef.current = false;
      setEnviando(false);
    }
  }, [mostrarPaso, manejarError]);

  const cancelar = useCallback(() => setFase('cancelado'), []);
  const volverAInstrucciones = useCallback(() => setFase('instrucciones'), []);

  // ------------------------------------------------------------ Pasos 5 a 8: responder bloque
  const elegirOpcion = useCallback((preguntaAsignadaId: number, opcionId: number) => {
    setRespuestas((r) => ({ ...r, [preguntaAsignadaId]: opcionId }));
    setFaltantes((f) => f.filter((id) => id !== preguntaAsignadaId));
  }, []);

  const enviarBloque = useCallback(async () => {
    if (!paso || paso.numeroBloque === null) return;

    // Con el tiempo agotado no se valida localmente el 6.A: se envía el bloque tal como está
    // para que el backend registre el vencimiento (1.A, el cuestionario pasa a INCOMPLETO).
    const tiempoAgotado = segundos === 0;

    // 6.A - se indica qué preguntas faltan y se conservan las respuestas ya marcadas
    const sinResponder = paso.preguntas.filter((p) => respuestas[p.preguntaAsignadaId] === undefined);
    if (!tiempoAgotado && sinResponder.length > 0) {
      setFaltantes(sinResponder.map((p) => p.preguntaAsignadaId));
      setMensaje(
        `Existen preguntas sin responder: ${sinResponder.map((p) => `Pregunta ${p.nroOrden}`).join(', ')}.`
      );
      emitirAlertaSonora();
      return;
    }

    if (enCursoRef.current) return;
    enCursoRef.current = true;
    setEnviando(true);
    setMensaje(null);
    try {
      const siguiente = await completarCuestionarioService.responderBloque(
        paso.cuestionarioId,
        paso.numeroBloque,
        paso.preguntas
          .filter((p) => respuestas[p.preguntaAsignadaId] !== undefined)
          .map((p) => ({ preguntaAsignadaId: p.preguntaAsignadaId, opcionId: respuestas[p.preguntaAsignadaId] }))
      );
      mostrarPaso(siguiente);
    } catch (error) {
      manejarError(error);
    } finally {
      enCursoRef.current = false;
      setEnviando(false);
    }
  }, [paso, respuestas, segundos, mostrarPaso, manejarError]);

  return {
    fase,
    enviando,
    mensaje,
    instrucciones,
    resumen,
    paso,
    respuestas,
    faltantes,
    segundos,
    aceptar,
    cancelar,
    volverAInstrucciones,
    elegirOpcion,
    enviarBloque,
  };
}
