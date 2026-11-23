'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError } from '@/services/apiClient';
import { evaluarCandidatosService } from '../services/evaluarCandidatosService';
import { guardarArchivoComo } from '../services/guardarArchivo';
import {
  ArchivoExcel,
  CandidatoBusqueda,
  CandidatoClave,
  EvaluacionCreada,
  FiltrosCandidatos,
  PuestoDetalle,
  PuestoOpcion,
} from '../types';

/** Pasos del wizard: 0 = Búsqueda de candidatos, 1 = Función o puesto, 2 = Candidatos a evaluar. */
export type PasoWizard = 0 | 1 | 2;

/** Mensaje modal que el sistema presenta al actor. */
export interface MensajeModal {
  tipo: 'error' | 'advertencia' | 'exito' | 'pregunta';
  titulo: string;
  mensaje: string;
  detalle?: string[];
  /** Qué hacer cuando el actor presiona "Aceptar" (o "Sí" en las preguntas). */
  accion: 'cerrar' | 'finalizar-cu' | 'exportar';
}

const mensajeDeError = (error: unknown): string =>
  error instanceof ApiError || error instanceof Error ? error.message : 'Ocurrió un error inesperado';

/**
 * Estado y lógica de la pantalla del CU 025 - Evaluar a Candidatos.
 */
export function useEvaluarCandidatos() {
  const [paso, setPaso] = useState<PasoWizard>(0);
  const [cargando, setCargando] = useState(false);
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null);
  const [modal, setModal] = useState<MensajeModal | null>(null);

  // Paso 1: búsqueda y selección de candidatos
  const [resultadosBusqueda, setResultadosBusqueda] = useState<CandidatoBusqueda[]>([]);
  const [candidatosAEvaluar, setCandidatosAEvaluar] = useState<CandidatoBusqueda[]>([]);

  // Paso 2: función o puesto
  const [puestos, setPuestos] = useState<PuestoOpcion[]>([]);
  const [puestoCodigo, setPuestoCodigo] = useState<string>('');
  const [puestoDetalle, setPuestoDetalle] = useState<PuestoDetalle | null>(null);
  const [cargandoPuesto, setCargandoPuesto] = useState(false);

  // Paso 3: candidatos con claves y evaluación guardada
  const [candidatosConClave, setCandidatosConClave] = useState<CandidatoClave[]>([]);
  const [evaluacion, setEvaluacion] = useState<EvaluacionCreada | null>(null);
  // El Excel se pide al backend apenas se guarda la evaluación, así el diálogo "Guardar como"
  // se abre inmediatamente cuando el actor responde "Sí" (el navegador exige que sea una acción directa).
  const archivoRef = useRef<Promise<ArchivoExcel> | null>(null);

  // ------------------------------------------------------------ Paso 2-3: búsqueda
  const buscarCandidatos = useCallback(async (filtros: FiltrosCandidatos = {}) => {
    setCargando(true);
    setErrorGeneral(null);
    try {
      setResultadosBusqueda(await evaluarCandidatosService.buscarCandidatos(filtros));
    } catch (error) {
      setErrorGeneral(mensajeDeError(error));
    } finally {
      setCargando(false);
    }
  }, []);

  // Carga inicial de la grilla con todos los candidatos
  useEffect(() => {
    let activo = true;
    evaluarCandidatosService
      .buscarCandidatos()
      .then((r) => activo && setResultadosBusqueda(r))
      .catch((e) => activo && setErrorGeneral(mensajeDeError(e)));
    return () => {
      activo = false;
    };
  }, []);

  // ------------------------------------------------------------ Paso 6: Siguiente
  const irAPuesto = useCallback(async () => {
    if (candidatosAEvaluar.length === 0) {
      // 4.A - No se han seleccionado candidatos
      setModal({
        tipo: 'advertencia',
        titulo: 'Candidatos a evaluar',
        mensaje: 'Debe seleccionar uno o más candidatos.',
        accion: 'cerrar',
      });
      return;
    }
    setCargando(true);
    setErrorGeneral(null);
    try {
      await evaluarCandidatosService.validarCandidatos(candidatosAEvaluar.map((c) => c.nroCandidato));
      if (puestos.length === 0) {
        setPuestos(await evaluarCandidatosService.listarPuestos());
      }
      setPaso(1);
    } catch (error) {
      if (error instanceof ApiError && error.status === 422) {
        // 6.A - Candidatos con cuestionarios activos o en proceso
        setModal({
          tipo: 'advertencia',
          titulo: 'No es posible evaluar a todos los candidatos',
          mensaje: error.message,
          accion: 'cerrar',
        });
      } else {
        setErrorGeneral(mensajeDeError(error));
      }
    } finally {
      setCargando(false);
    }
  }, [candidatosAEvaluar, puestos.length]);

  // ------------------------------------------------------------ Paso 8-9: selección de puesto
  const seleccionarPuesto = useCallback(async (codigo: string) => {
    setPuestoCodigo(codigo);
    setPuestoDetalle(null);
    setErrorGeneral(null);
    if (!codigo) return;

    setCargandoPuesto(true);
    try {
      const [detalle, validacion] = await Promise.all([
        evaluarCandidatosService.obtenerPuesto(codigo),
        evaluarCandidatosService.validarPuesto(codigo),
      ]);
      if (!validacion.evaluable) {
        // 9.A - Competencias sin factores válidos para evaluar
        setModal({
          tipo: 'error',
          titulo: 'Competencias que no pueden ser evaluadas',
          mensaje: `La función "${detalle.nombre}" posee competencias que no tienen al menos un factor con 2 o más preguntas:`,
          detalle: validacion.competenciasNoEvaluables,
          accion: 'finalizar-cu',
        });
        return;
      }
      setPuestoDetalle(detalle);
    } catch (error) {
      setErrorGeneral(mensajeDeError(error));
    } finally {
      setCargandoPuesto(false);
    }
  }, []);

  // ------------------------------------------------------------ Paso 10: Siguiente
  const irAConfirmacion = useCallback(async () => {
    if (!puestoCodigo || !puestoDetalle) {
      // 10.A - No se seleccionó ninguna función
      setModal({
        tipo: 'advertencia',
        titulo: 'Función o puesto',
        mensaje: 'Debe seleccionar una función o puesto.',
        accion: 'cerrar',
      });
      return;
    }
    setCargando(true);
    setErrorGeneral(null);
    try {
      const conClave = await evaluarCandidatosService.prepararEvaluacion(
        candidatosAEvaluar.map((c) => c.nroCandidato),
        puestoCodigo
      );
      setCandidatosConClave(conClave);
      setPaso(2);
    } catch (error) {
      if (error instanceof ApiError && error.status === 422) {
        setModal({ tipo: 'advertencia', titulo: 'Atención', mensaje: error.message, accion: 'cerrar' });
      } else {
        setErrorGeneral(mensajeDeError(error));
      }
    } finally {
      setCargando(false);
    }
  }, [candidatosAEvaluar, puestoCodigo, puestoDetalle]);

  // ------------------------------------------------------------ Paso 12-13: Finalizar
  const finalizar = useCallback(async () => {
    if (evaluacion) return;
    setCargando(true);
    setErrorGeneral(null);
    try {
      const creada = await evaluarCandidatosService.evaluarCandidatos(puestoCodigo, candidatosConClave);
      setEvaluacion(creada);
      setCandidatosConClave(creada.candidatos);
      archivoRef.current = evaluarCandidatosService.exportarExcel(
        creada.evaluacionId,
        `${creada.puestoNombre}.xls`
      );
      archivoRef.current.catch(() => {
        /* el error se informa al intentar exportar */
      });
      setModal({
        tipo: 'pregunta',
        titulo: 'Evaluación guardada',
        mensaje: '¿Desea exportar la lista de candidatos con sus claves a un archivo Excel?',
        accion: 'exportar',
      });
    } catch (error) {
      if (error instanceof ApiError && error.status === 422) {
        setModal({ tipo: 'advertencia', titulo: 'No se pudo guardar la evaluación', mensaje: error.message, accion: 'cerrar' });
      } else {
        setErrorGeneral(mensajeDeError(error));
      }
    } finally {
      setCargando(false);
    }
  }, [evaluacion, puestoCodigo, candidatosConClave]);

  // ------------------------------------------------------------ Pasos 14-17: exportar
  const exportarExcel = useCallback(async () => {
    if (!evaluacion) return;
    setModal(null);
    setCargando(true);
    try {
      let archivo: ArchivoExcel;
      try {
        archivo = await (archivoRef.current ??
          evaluarCandidatosService.exportarExcel(evaluacion.evaluacionId, `${evaluacion.puestoNombre}.xls`));
      } catch (error) {
        archivoRef.current = null; // se reintenta en el próximo intento
        throw error;
      }
      const resultado = await guardarArchivoComo(archivo.contenido, archivo.nombreArchivo);
      if (resultado === 'cancelado') {
        // 16.A - Cancela el guardado: vuelve a preguntar si desea exportar (paso 14)
        setModal({
          tipo: 'pregunta',
          titulo: 'Exportación cancelada',
          mensaje: '¿Desea exportar la lista de candidatos con sus claves a un archivo Excel?',
          accion: 'exportar',
        });
        return;
      }
      setModal({
        tipo: 'exito',
        titulo: 'Archivo guardado',
        mensaje: `Se guardó el archivo "${archivo.nombreArchivo}".`,
        accion: 'finalizar-cu',
      });
    } catch (error) {
      setModal({
        tipo: 'error',
        titulo: 'No se pudo exportar el archivo',
        mensaje: mensajeDeError(error),
        accion: 'exportar',
      });
    } finally {
      setCargando(false);
    }
  }, [evaluacion]);

  // ------------------------------------------------------------ Navegación
  const volver = useCallback(() => {
    setErrorGeneral(null);
    if (paso === 2) {
      setCandidatosConClave([]);
      setPaso(1);
    } else if (paso === 1) {
      setPaso(0);
    }
  }, [paso]);

  const cerrarModal = useCallback(() => setModal(null), []);

  return {
    paso,
    cargando,
    cargandoPuesto,
    errorGeneral,
    setErrorGeneral,
    modal,
    cerrarModal,
    // paso 1
    resultadosBusqueda,
    candidatosAEvaluar,
    setCandidatosAEvaluar,
    buscarCandidatos,
    irAPuesto,
    // paso 2
    puestos,
    puestoCodigo,
    puestoDetalle,
    seleccionarPuesto,
    irAConfirmacion,
    // paso 3
    candidatosConClave,
    evaluacion,
    finalizar,
    exportarExcel,
    volver,
  };
}
