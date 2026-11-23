'use client';

import React, { useEffect, useRef } from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { MensajeModal } from '../hooks/useEvaluarCandidatos';

export interface ModalMensajeProps {
  mensaje: MensajeModal | null;
  /** Botón principal: "Aceptar" (o "Sí" en las preguntas). */
  onAceptar: () => void;
  /** Botón "No" de las preguntas. */
  onRechazar?: () => void;
}

const ESTILOS: Record<MensajeModal['tipo'], { icono: string; fondo: string; color: string }> = {
  error: { icono: 'error', fondo: 'bg-red-100', color: 'text-[#D32F2F]' },
  advertencia: { icono: 'warning', fondo: 'bg-amber-100', color: 'text-amber-700' },
  exito: { icono: 'check_circle', fondo: 'bg-[#E8F5E9]', color: 'text-[#1B8354]' },
  pregunta: { icono: 'download', fondo: 'bg-[#EADDFF]', color: 'text-[#3F2581]' },
};

/**
 * Mensaje modal del CU 025 (flujos alternativos y pregunta de exportación a Excel).
 */
export const ModalMensaje: React.FC<ModalMensajeProps> = ({ mensaje, onAceptar, onRechazar }) => {
  const botonPrincipal = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (mensaje) {
      const t = setTimeout(() => botonPrincipal.current?.focus(), 50);
      return () => clearTimeout(t);
    }
  }, [mensaje]);

  if (!mensaje) return null;

  const estilo = ESTILOS[mensaje.tipo];
  const esPregunta = mensaje.tipo === 'pregunta';

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="cu025-modal-titulo"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs"
    >
      <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl border-2 border-[#D0BCFF] p-6 flex flex-col items-center text-center gap-4">
        <div className={`w-14 h-14 rounded-full flex items-center justify-center ${estilo.fondo} ${estilo.color}`}>
          <Icon name={estilo.icono} size={30} />
        </div>

        <div className="flex flex-col gap-2">
          <h2 id="cu025-modal-titulo" className="text-xl font-bold text-[#3F2581]">
            {mensaje.titulo}
          </h2>
          <p className="text-base text-gray-800 leading-relaxed">{mensaje.mensaje}</p>
          {mensaje.detalle && mensaje.detalle.length > 0 && (
            <ul className="mt-1 text-sm text-left list-disc list-inside text-gray-700 bg-gray-50 rounded-lg p-3">
              {mensaje.detalle.map((d) => (
                <li key={d} className="font-semibold">
                  {d}
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className={`grid gap-3 w-full pt-2 ${esPregunta ? 'grid-cols-2' : 'grid-cols-1'}`}>
          {esPregunta && (
            <Button type="button" variant="secondary" onClick={onRechazar} className="w-full">
              No
            </Button>
          )}
          <Button ref={botonPrincipal} type="button" variant="primary" onClick={onAceptar} className="w-full">
            {esPregunta ? 'Sí, exportar' : 'Aceptar'}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ModalMensaje;
