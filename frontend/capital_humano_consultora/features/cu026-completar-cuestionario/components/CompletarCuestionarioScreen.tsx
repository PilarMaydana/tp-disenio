'use client';

import React from 'react';
import { useAutoDismissError } from '@/hooks/useAutoDismissError';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { useAuth } from '@/context/AuthContext';
import { useCompletarCuestionario } from '../hooks/useCompletarCuestionario';
import { PreguntaCuestionario } from '../types';

const formatoTiempo = (segundos: number | null): string => {
  if (segundos === null) return '--:--';
  const m = Math.floor(segundos / 60);
  const s = segundos % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
};

const Tarjeta: React.FC<{ children: React.ReactNode; className?: string }> = ({ children, className = '' }) => (
  <div className={`bg-white p-6 md:p-8 rounded-2xl border border-[#D0BCFF] shadow-xs flex flex-col gap-5 ${className}`}>
    {children}
  </div>
);

const Aviso: React.FC<{ tipo: 'error' | 'exito' | 'info'; icono: string; titulo: string; children?: React.ReactNode }> = ({
  tipo,
  icono,
  titulo,
  children,
}) => {
  const colores = {
    error: 'bg-red-100 text-[#D32F2F]',
    exito: 'bg-[#E8F5E9] text-[#1B8354]',
    info: 'bg-[#EADDFF] text-[#3F2581]',
  }[tipo];
  return (
    <div className="flex flex-col items-center text-center gap-3 py-4">
      <div className={`w-16 h-16 rounded-full flex items-center justify-center ${colores}`}>
        <Icon name={icono} size={34} />
      </div>
      <h2 className="text-2xl font-bold text-[#3F2581]">{titulo}</h2>
      {children}
    </div>
  );
};

const Pregunta: React.FC<{
  pregunta: PreguntaCuestionario;
  elegida?: number;
  falta: boolean;
  deshabilitada: boolean;
  onElegir: (opcionId: number) => void;
}> = ({ pregunta, elegida, falta, deshabilitada, onElegir }) => (
  <fieldset
    className={`p-5 rounded-xl border-2 transition-colors ${
      falta ? 'border-red-400 bg-red-50' : 'border-[#E0E0E0] bg-white'
    }`}
  >
    <legend className="px-2 text-sm font-bold text-[#3F2581]">Pregunta {pregunta.nroOrden}</legend>
    <p className="text-base font-medium text-[#333333] mb-4">{pregunta.enunciado}</p>
    <div className="flex flex-col gap-2">
      {pregunta.opciones.map((o) => (
        <label
          key={o.id}
          className={`flex items-center gap-3 p-3 rounded-lg border cursor-pointer transition-colors ${
            elegida === o.id ? 'border-[#3F2581] bg-[#F4F0FF]' : 'border-[#E0E0E0] hover:border-[#BDBEE0]'
          }`}
        >
          <input
            type="radio"
            name={`pregunta-${pregunta.preguntaAsignadaId}`}
            value={o.id}
            checked={elegida === o.id}
            disabled={deshabilitada}
            onChange={() => onElegir(o.id)}
            className="w-4 h-4 accent-[#3F2581]"
          />
          <span className="text-sm text-[#333333]">{o.descripcion}</span>
        </label>
      ))}
    </div>
    {falta && <p className="mt-3 text-sm font-semibold text-[#D32F2F]">Debe responder esta pregunta.</p>}
  </fieldset>
);

/**
 * Pantalla del CU 026 - Completar cuestionario (actor: Candidato).
 */
export const CompletarCuestionarioScreen: React.FC = () => {
  const cu = useCompletarCuestionario();
  const mostrarMensaje = useAutoDismissError(cu.mensaje, cu.limpiarMensaje);
  const { logout } = useAuth();

  const encabezado = (cu.paso || cu.resumen) && (
    <div className="flex flex-col">
      <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">Cuestionario de evaluación</h1>
      <p className="text-sm md:text-base text-[#555555] mt-1 font-medium">
        {(cu.paso ?? cu.resumen)?.puestoNombre} — {(cu.paso ?? cu.resumen)?.empresa}
      </p>
    </div>
  );

  return (
    <div className="flex flex-col gap-6 w-full max-w-3xl mx-auto">
      {encabezado}

      {cu.fase === 'cargando' && <p className="p-12 text-center text-[#757575]">Cargando cuestionario…</p>}

      {cu.fase === 'error' && (
        <Tarjeta>
          <Aviso tipo="error" icono="error" titulo="No se pudo cargar el cuestionario">
            <p className="text-[#555555]">{cu.mensaje}</p>
          </Aviso>
        </Tarjeta>
      )}

      {cu.fase === 'sin-cuestionario' && (
        <Tarjeta>
          <Aviso tipo="info" icono="assignment" titulo="No posee un cuestionario pendiente">
            <p className="text-[#555555]">No tiene un cuestionario activo o en proceso en este momento.</p>
          </Aviso>
        </Tarjeta>
      )}

      {/* ------------------------------------------------ Pasos 1 y 2: Instrucciones */}
      {cu.fase === 'instrucciones' && cu.instrucciones && cu.resumen && (
        <Tarjeta>
          <h2 className="text-xl font-bold text-[#3F2581]">Cuestionario – Instrucciones</h2>
          <p className="text-[#333333] leading-relaxed text-justify">
            Lea cada pregunta con atención y elija una única opción. Las preguntas se presentan en bloques: debe responder todas las preguntas del bloque para avanzar y, una vez que avanza, no puede volver a un bloque anterior ni modificar sus respuestas. Puede salir y continuar más tarde mientras no se supere el tiempo máximo ni la cantidad máxima de ingresos.
          </p>
          <ul className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
            <li className="p-3 bg-[#F4F0FF] rounded-xl">
              <span className="block text-[#757575]">Tiempo máximo</span>
              <strong className="text-[#3F2581]">{cu.resumen.tiempoMaximoMinutos ?? cu.instrucciones.tiempoMaximoMinutos} minutos</strong>
            </li>
            <li className="p-3 bg-[#F4F0FF] rounded-xl">
              <span className="block text-[#757575]">Ingresos realizados</span>
              <strong className="text-[#3F2581]">
                {cu.resumen.cantidadAccesos} de {cu.resumen.cantidadMaximaAccesos ?? cu.instrucciones.cantidadMaximaAccesos}
              </strong>
            </li>
          </ul>
          {cu.resumen.estado === 'EN_PROCESO' && (
            <p className="p-3 rounded-xl bg-amber-50 border border-amber-200 text-sm text-amber-800">
              Ya comenzó este cuestionario ({cu.resumen.preguntasRespondidas} de {cu.resumen.totalPreguntas} preguntas
              respondidas). Al aceptar continuará en el bloque pendiente
              {cu.resumen.segundosRestantes !== null && ` (le quedan ${formatoTiempo(cu.resumen.segundosRestantes)} min)`}.
            </p>
          )}
          {cu.mensaje && <p className="text-sm font-medium text-[#D32F2F]">{cu.mensaje}</p>}
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={cu.cancelar} disabled={cu.enviando}>
              Cancelar
            </Button>
            <Button type="button" variant="primary" onClick={cu.aceptar} isLoading={cu.enviando} autoFocus>
              Aceptar
            </Button>
          </div>
        </Tarjeta>
      )}

      {/* ------------------------------------------------ 2.A: Cancelar */}
      {cu.fase === 'cancelado' && (
        <Tarjeta>
          <Aviso tipo="info" icono="clock" titulo="Cuestionario sin comenzar">
            <p className="text-[#555555] max-w-md">
              No se realizaron cambios en su cuestionario. Puede volver a ingresar más tarde para completarlo.
            </p>
          </Aviso>
          <div className="flex justify-center gap-3">
            <Button type="button" variant="secondary" onClick={logout}>
              Cerrar sesión
            </Button>
            <Button type="button" variant="primary" onClick={cu.volverAInstrucciones}>
              Ver instrucciones
            </Button>
          </div>
        </Tarjeta>
      )}

      {/* ------------------------------------------------ Pasos 4 a 7: wizard */}
      {cu.fase === 'wizard' && cu.paso && (
        <>
          <div className="flex flex-wrap items-center justify-between gap-3 p-4 bg-white rounded-2xl border border-[#D0BCFF]">
            <div className="text-sm text-[#555555]">
              <strong className="text-[#3F2581] text-base">
                Bloque {cu.paso.numeroBloque} de {cu.paso.totalBloques}
              </strong>
              <span className="ml-2">
                ({cu.paso.preguntasRespondidas} de {cu.paso.totalPreguntas} preguntas respondidas)
              </span>
            </div>
            <div
              className={`flex items-center gap-2 font-mono font-bold ${
                cu.segundos !== null && cu.segundos < 300 ? 'text-[#D32F2F]' : 'text-[#3F2581]'
              }`}
              title="Tiempo restante para completar el cuestionario"
            >
              <Icon name="timer" size={18} />
              {formatoTiempo(cu.segundos)}
            </div>
          </div>

          <div className="w-full h-2 bg-[#EADDFF] rounded-full overflow-hidden">
            <div
              className="h-full bg-[#3F2581] transition-all"
              style={{ width: `${(cu.paso.preguntasRespondidas / Math.max(cu.paso.totalPreguntas, 1)) * 100}%` }}
            />
          </div>

          {mostrarMensaje && cu.mensaje && (
            <div role="alert" className="p-4 bg-red-50 border-2 border-red-300 rounded-xl flex items-start gap-3">
              <Icon name="error" size={22} className="text-[#D32F2F] shrink-0" />
              <p className="flex-1 text-sm font-medium text-red-800">{cu.mensaje}</p>
              <button type="button" onClick={cu.limpiarMensaje} aria-label="Cerrar mensaje de error" title="Cerrar mensaje de error" className="shrink-0 rounded p-1 text-red-500 hover:bg-red-100 hover:text-red-700"><Icon name="close" size={18} aria-hidden="true" /></button>
            </div>
          )}

          {cu.segundos === 0 && (
            <p className="p-3 rounded-xl bg-red-50 border border-red-200 text-sm text-red-800">
              Se agotó el tiempo máximo para completar el cuestionario. Ya no es posible responder;
              presione «{cu.paso.numeroBloque === cu.paso.totalBloques ? 'Finalizar' : 'Siguiente'}» para cerrarlo.
            </p>
          )}

          <div className="flex flex-col gap-4">
            {cu.paso.preguntas.map((p) => (
              <Pregunta
                key={p.preguntaAsignadaId}
                pregunta={p}
                elegida={cu.respuestas[p.preguntaAsignadaId]}
                falta={cu.faltantes.includes(p.preguntaAsignadaId)}
                deshabilitada={cu.enviando || cu.segundos === 0}
                onElegir={(opcionId) => cu.elegirOpcion(p.preguntaAsignadaId, opcionId)}
              />
            ))}
          </div>

          <p className="text-xs text-[#757575]">
            Una vez que avance al bloque siguiente no podrá volver a este bloque ni modificar sus respuestas.
          </p>

          <div className="flex justify-end">
            <Button
              type="button"
              variant="primary"
              onClick={cu.enviarBloque}
              isLoading={cu.enviando}
              rightIcon={cu.paso.numeroBloque === cu.paso.totalBloques ? 'check' : 'arrow_forward'}
            >
              {cu.paso.numeroBloque === cu.paso.totalBloques ? 'Finalizar' : 'Siguiente'}
            </Button>
          </div>
        </>
      )}

      {/* ------------------------------------------------ Paso 8: completado */}
      {cu.fase === 'completado' && (
        <Tarjeta>
          <Aviso tipo="exito" icono="check_circle" titulo="El cuestionario ha sido completado con éxito">
            <p className="text-[#555555] max-w-md">
              Gracias por completar la evaluación. La consultora se comunicará con usted.
            </p>
          </Aviso>
          <div className="flex justify-center">
            <Button type="button" variant="primary" onClick={logout}>
              Cerrar sesión
            </Button>
          </div>
        </Tarjeta>
      )}

      {/* ------------------------------------------------ 1.A / 1.B */}
      {cu.fase === 'no-disponible' && (
        <Tarjeta>
          <Aviso tipo="error" icono="warning" titulo="No es posible continuar con el cuestionario">
            <p className="text-[#555555] max-w-md">{cu.mensaje}</p>
          </Aviso>
          <div className="flex justify-center">
            <Button type="button" variant="primary" onClick={logout}>
              Cerrar sesión
            </Button>
          </div>
        </Tarjeta>
      )}
    </div>
  );
};

export default CompletarCuestionarioScreen;
