'use client';

import React, { useMemo, useState } from 'react';
import { Stepper } from '@/components/molecules/Stepper';
import { SearchFilterBar } from '@/components/molecules/SearchFilterBar';
import { NotchedField } from '@/components/molecules/NotchedField';
import { DualTransferList, TransferItem } from '@/components/organisms/DualTransferList';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import { DataTable, Column } from '@/components/organisms/DataTable';
import { Button } from '@/components/ui/Button';
import { Select } from '@/components/ui/Select';
import { Icon } from '@/components/ui/Icon';
import { useEvaluarCandidatos } from '../hooks/useEvaluarCandidatos';
import { CandidatoBusqueda, CandidatoClave } from '../types';
import { ModalMensaje } from './ModalMensaje';
import { useAutoDismissError } from '@/hooks/useAutoDismissError';

const PASOS = [
  { id: 'candidatos', label: 'Candidatos' },
  { id: 'puesto', label: 'Función o puesto' },
  { id: 'confirmar', label: 'Candidatos a evaluar' },
];

const TIPO_DOC: Record<string, string> = { DNI: 'DNI', LE: 'LE', LC: 'LC', PP: 'Pasaporte' };

const aItem = (c: CandidatoBusqueda): TransferItem => ({
  id: String(c.nroCandidato),
  label: `${c.apellido}, ${c.nombre}`,
  subtitle: `Nro. ${c.nroCandidato} · ${TIPO_DOC[c.tipoDocumento] ?? c.tipoDocumento} ${c.nroDocumento}`,
});

const COLUMNAS: Column<CandidatoClave>[] = [
  { header: 'Apellido', accessor: 'apellido' },
  { header: 'Nombre', accessor: 'nombre' },
  { header: 'Tipo documento', accessor: 'tipoDocumento', render: (c) => TIPO_DOC[c.tipoDocumento] ?? c.tipoDocumento },
  { header: 'Nro. documento', accessor: 'nroDocumento' },
  {
    header: 'Clave de ingreso',
    accessor: 'claveAcceso',
    render: (c) => (
      <span className="font-mono font-bold tracking-wider text-[#3F2581] bg-[#F4F0FF] px-2 py-1 rounded-md">
        {c.claveAcceso}
      </span>
    ),
  },
];

/**
 * Pantalla del CU 025 - Evaluar a Candidatos.
 *
 * Paso 1: Búsqueda de Candidatos (pasos 2 a 6 del CU).
 * Paso 2: Evaluación de Candidatos - función o puesto (pasos 7 a 10).
 * Paso 3: Candidatos a evaluar con sus claves (pasos 11 a 17).
 */
export const EvaluarCandidatosScreen: React.FC = () => {
  // Cada vez que el CU finaliza (exportó, no exportó, 9.A o Cancelar) se vuelve a montar la
  // pantalla desde el paso 1, con todos los datos limpios: el consultor permanece en el módulo
  // de evaluaciones en lugar de ser redirigido a la pantalla principal (Alta de Puesto).
  const [ejecucion, setEjecucion] = useState(0);
  return <EvaluarCandidatos key={ejecucion} onFinalizar={() => setEjecucion((n) => n + 1)} />;
};

const EvaluarCandidatos: React.FC<{ onFinalizar: () => void }> = ({ onFinalizar }) => {
  const cu = useEvaluarCandidatos();
  const mostrarErrorGeneral = useAutoDismissError(cu.errorGeneral, () => cu.setErrorGeneral(null));
  const finalizarCU = () => {
    window.scrollTo({ top: 0 });
    onFinalizar();
  };

  // Mapa nroCandidato -> candidato, para recuperar los datos desde la lista de transferencia
  const porNro = useMemo(() => {
    const mapa = new Map<string, CandidatoBusqueda>();
    [...cu.resultadosBusqueda, ...cu.candidatosAEvaluar].forEach((c) => mapa.set(String(c.nroCandidato), c));
    return mapa;
  }, [cu.resultadosBusqueda, cu.candidatosAEvaluar]);

  const itemsDisponibles = cu.resultadosBusqueda.map(aItem);
  const itemsSeleccionados = cu.candidatosAEvaluar.map(aItem);

  const alCambiarSeleccion = (items: TransferItem[]) => {
    cu.setCandidatosAEvaluar(
      items.map((i) => porNro.get(i.id)).filter((c): c is CandidatoBusqueda => Boolean(c))
    );
  };

  const alAceptarModal = () => {
    const accion = cu.modal?.accion;
    if (accion === 'exportar') {
      cu.exportarExcel();
      return;
    }
    cu.cerrarModal();
    if (accion === 'finalizar-cu') {
      finalizarCU();
    }
  };

  const alRechazarModal = () => {
    // 14.A - No exportar: el CU finaliza
    cu.cerrarModal();
    finalizarCU();
  };

  return (
    <div className="flex flex-col gap-6 w-full max-w-6xl mx-auto">
      <div className="flex flex-col">
        <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">Evaluar Candidatos</h1>
        <p className="text-sm text-[#757575] mt-1">
          Seleccione los candidatos y la función o puesto en la que serán evaluados. El sistema genera un cuestionario y
          una clave de ingreso para cada uno.
        </p>
      </div>

      <Stepper steps={PASOS} currentStep={cu.paso} />

      {mostrarErrorGeneral && cu.errorGeneral && (
        <div role="alert" className="p-4 bg-red-50 border-2 border-red-300 rounded-xl flex items-start gap-3">
          <Icon name="error" size={22} className="text-[#D32F2F] shrink-0" />
          <p className="flex-1 text-sm font-medium text-red-800">{cu.errorGeneral}</p>
          <button
            type="button"
            onClick={() => cu.setErrorGeneral(null)}
            className="p-1 text-red-500 hover:text-red-700"
            title="Cerrar"
          >
            <Icon name="close" size={18} />
          </button>
        </div>
      )}

      {/* ---------------------------------------------------------------- Paso 1 */}
      {cu.paso === 0 && (
        <div className="flex flex-col gap-6">
          <SearchFilterBar
            fields={[
              { key: 'apellido', label: 'Apellido', placeholder: 'Ej: Romero' },
              { key: 'nombre', label: 'Nombre', placeholder: 'Ej: Agustín' },
              { key: 'nroCandidato', label: 'Nro. Candidato', placeholder: 'Ej: 4' },
            ]}
            onSearch={(f) => cu.buscarCandidatos({ apellido: f.apellido, nombre: f.nombre, nroCandidato: f.nroCandidato })}
          />
          <DualTransferList
            availableTitle="Candidatos encontrados"
            selectedTitle="Candidatos a evaluar"
            availableItems={itemsDisponibles}
            selectedItems={itemsSeleccionados}
            onChange={alCambiarSeleccion}
            withWeights={false}
          />
        </div>
      )}

      {/* ---------------------------------------------------------------- Paso 2 */}
      {cu.paso === 1 && (
        <div className="bg-white p-8 rounded-2xl border border-[#D0BCFF] shadow-xs flex flex-col gap-6">
          <div>
            <h2 className="text-xl font-bold text-[#3F2581]">Evaluación de Candidatos</h2>
            <p className="text-sm text-[#757575] mt-1">
              Seleccione la función o puesto en la cual desea evaluar a los {cu.candidatosAEvaluar.length} candidato(s)
              seleccionado(s).
            </p>
          </div>

          <NotchedField label="Función o puesto" required>
            <Select
              options={cu.puestos.map((p) => ({ value: p.codigo, label: `${p.nombre} — ${p.empresa}` }))}
              placeholder="Seleccione la función o puesto"
              value={cu.puestoCodigo}
              onChange={(e) => cu.seleccionarPuesto(e.target.value)}
            />
          </NotchedField>

          {cu.cargandoPuesto && <p className="text-sm text-[#757575]">Cargando competencias…</p>}

          {cu.puestoDetalle && (
            <div className="p-4 bg-[#F4F0FF] rounded-xl border border-[#D0BCFF]">
              <h3 className="text-sm font-bold text-[#3F2581] mb-3">
                Competencias requeridas para {cu.puestoDetalle.nombre} ({cu.puestoDetalle.empresa})
              </h3>
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-[#3F2581]">
                    <th className="py-1 font-semibold">Competencia</th>
                    <th className="py-1 font-semibold text-right">Ponderación requerida</th>
                  </tr>
                </thead>
                <tbody>
                  {cu.puestoDetalle.caracteristicas.map((c) => (
                    <tr key={c.competenciaId} className="border-t border-[#D0BCFF]/60">
                      <td className="py-2 text-[#333333]">{c.nombreCompetencia}</td>
                      <td className="py-2 text-right font-semibold text-[#3F2581]">{c.ponderacion}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* ---------------------------------------------------------------- Paso 3 */}
      {cu.paso === 2 && (
        <div className="bg-white p-8 rounded-2xl border border-[#D0BCFF] shadow-xs flex flex-col gap-6">
          <div>
            <h2 className="text-xl font-bold text-[#3F2581]">Candidatos a evaluar</h2>
            <p className="text-sm text-[#757575] mt-1">
              Función o puesto: <strong>{cu.puestoDetalle?.nombre}</strong> ({cu.puestoDetalle?.empresa}).{' '}
              {cu.evaluacion
                ? 'La evaluación fue guardada: los cuestionarios quedaron en estado activo.'
                : 'Al presionar «Finalizar» se generan los cuestionarios con estas claves de ingreso.'}
            </p>
          </div>
          <DataTable
            columns={COLUMNAS}
            data={cu.candidatosConClave}
            keyExtractor={(c) => String(c.nroCandidato)}
            variant="soft-purple"
          />
        </div>
      )}

      {/* ---------------------------------------------------------------- Acciones */}
      {!cu.evaluacion && (
        <ActionToolbar align="between">
          {cu.paso === 0 ? (
            <Button type="button" variant="secondary" onClick={finalizarCU}>
              Cancelar
            </Button>
          ) : (
            <Button type="button" variant="secondary" onClick={cu.volver} leftIcon="arrow_back" disabled={cu.cargando}>
              Anterior
            </Button>
          )}

          <div className="flex gap-3">
            {cu.paso === 2 && (
              // 12.A - Cancelar: vuelve a la pantalla principal sin guardar
              <Button type="button" variant="secondary" onClick={finalizarCU} disabled={cu.cargando}>
                Cancelar
              </Button>
            )}
            {cu.paso === 0 && (
              <Button type="button" variant="primary" onClick={cu.irAPuesto} isLoading={cu.cargando} rightIcon="arrow_forward">
                Siguiente
              </Button>
            )}
            {cu.paso === 1 && (
              <Button
                type="button"
                variant="primary"
                onClick={cu.irAConfirmacion}
                isLoading={cu.cargando}
                disabled={cu.cargandoPuesto}
                rightIcon="arrow_forward"
              >
                Siguiente
              </Button>
            )}
            {cu.paso === 2 && (
              <Button type="button" variant="primary" onClick={cu.finalizar} isLoading={cu.cargando} rightIcon="check">
                Finalizar
              </Button>
            )}
          </div>
        </ActionToolbar>
      )}

      <ModalMensaje mensaje={cu.modal} onAceptar={alAceptarModal} onRechazar={alRechazarModal} />
    </div>
  );
};

export default EvaluarCandidatosScreen;
