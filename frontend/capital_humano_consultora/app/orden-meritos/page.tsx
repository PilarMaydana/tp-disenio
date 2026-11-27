'use client';

import { useEffect, useMemo, useState } from 'react';
import { AlertCircle, ArrowLeft, ArrowRight, Award, Building2, CheckCircle2, ChevronDown, ClipboardList, FileSpreadsheet, FileText, MoreHorizontal, Printer, Search, SlidersHorizontal, X } from 'lucide-react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { Button } from '@/components/ui/Button';
import { CandidatoOrdenMerito, EvaluacionOrdenMerito, PuestoOrdenMerito, ordenMeritoService } from '@/services/ordenMeritoService';
import { Empresa } from '@/types/puesto';
import { useAutoDismissError } from '@/hooks/useAutoDismissError';

type Candidato = CandidatoOrdenMerito & { documento: string; estado: 'apto' | 'minimo' | 'incompleto' | 'pendiente'; puntaje: number | null; inicio: string; fin?: string };

const inputClass = 'w-full rounded-lg border border-[#3F2581]/50 bg-white px-4 py-2.5 text-base text-[#000000] outline-none transition-colors focus:border-[#3F2581] focus:ring-2 focus:ring-[#3F2581]/20';
const panelClass = 'rounded-xl border border-[#E3DFE9] bg-white shadow-[0_3px_12px_rgba(30,20,50,0.05)]';

function OrdenMeritosContent() {
  const [filtros, setFiltros] = useState({ codigo: '', nombrePuesto: '', empresaId: '' });
  const [campoSugerencias, setCampoSugerencias] = useState<'codigo' | 'nombrePuesto' | null>(null);
  const [sugerencias, setSugerencias] = useState<PuestoOrdenMerito[]>([]);
  const [consultaSugerencias, setConsultaSugerencias] = useState('');
  const [puestos, setPuestos] = useState<PuestoOrdenMerito[]>([]);
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [evaluaciones, setEvaluaciones] = useState<EvaluacionOrdenMerito[]>([]);
  const [candidatos, setCandidatos] = useState<Candidato[]>([]);
  const [busco, setBusco] = useState(false);
  const [buscando, setBuscando] = useState(false);
  const [puestoSeleccionado, setPuestoSeleccionado] = useState<PuestoOrdenMerito | null>(null);
  const [puestoReporte, setPuestoReporte] = useState<PuestoOrdenMerito | null>(null);
  const [menuExportacionAbierto, setMenuExportacionAbierto] = useState(false);
  const [exportando, setExportando] = useState(false);
  const [evaluacionesSeleccionadas, setEvaluacionesSeleccionadas] = useState<string[]>([]);
  const [filtroNombreEvaluacion, setFiltroNombreEvaluacion] = useState('');
  const [modoFechaEvaluacion, setModoFechaEvaluacion] = useState<'dia' | 'rango'>('dia');
  const [fechaEvaluacion, setFechaEvaluacion] = useState('');
  const [fechaDesdeEvaluaciones, setFechaDesdeEvaluaciones] = useState('');
  const [fechaHastaEvaluaciones, setFechaHastaEvaluaciones] = useState('');
  const [filtroEstadoEvaluacion, setFiltroEstadoEvaluacion] = useState('');
  const [filtrosEvaluacionAbiertos, setFiltrosEvaluacionAbiertos] = useState(false);
  const [fechaMaxima, setFechaMaxima] = useState('');
  const [error, setError] = useState('');
  const mostrarError = useAutoDismissError(error, () => setError(''));

  const terminoSugerencias = campoSugerencias ? filtros[campoSugerencias].trim() : '';
  const claveSugerencias = campoSugerencias && terminoSugerencias
    ? `${campoSugerencias}:${terminoSugerencias.toLocaleLowerCase()}`
    : '';

  useEffect(() => {
    const timer = window.setTimeout(() => {
      const hoy = new Date();
      setFechaMaxima(`${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`);
    }, 0);
    return () => window.clearTimeout(timer);
  }, []);

  useEffect(() => {
    if (!campoSugerencias || !terminoSugerencias) return;

    let vigente = true;
    const temporizador = window.setTimeout(async () => {
      try {
        const lista = await ordenMeritoService.buscarPuestos(
          campoSugerencias === 'codigo'
            ? { codigo: terminoSugerencias }
            : { nombrePuesto: terminoSugerencias }
        );
        if (vigente) {
          setSugerencias(lista.slice(0, 8));
          setConsultaSugerencias(`${campoSugerencias}:${terminoSugerencias.toLocaleLowerCase()}`);
        }
      } catch {
        if (vigente) {
          setSugerencias([]);
          setConsultaSugerencias(`${campoSugerencias}:${terminoSugerencias.toLocaleLowerCase()}`);
        }
      }
    }, 250);

    return () => {
      vigente = false;
      window.clearTimeout(temporizador);
    };
  }, [campoSugerencias, terminoSugerencias]);

  const resultados = useMemo(() => puestos.filter((puesto) => {
    const codigo = filtros.codigo.trim().toLowerCase();
    const nombre = filtros.nombrePuesto.trim().toLowerCase();
    return (!codigo || puesto.codigo.toLowerCase().includes(codigo)) &&
      (!nombre || puesto.nombrePuesto.toLowerCase().includes(nombre)) &&
      (!filtros.empresaId || puesto.empresaId === filtros.empresaId);
  }), [puestos, filtros]);

  const evaluacionesFiltradas = evaluaciones.filter((evaluacion) =>
    evaluacion.proyecto.toLocaleLowerCase().includes(filtroNombreEvaluacion.trim().toLocaleLowerCase()) &&
    (modoFechaEvaluacion === 'dia'
      ? (!fechaEvaluacion || evaluacion.fechaISO === fechaEvaluacion)
      : ((!fechaDesdeEvaluaciones || evaluacion.fechaISO >= fechaDesdeEvaluaciones) &&
        (!fechaHastaEvaluaciones || evaluacion.fechaISO <= fechaHastaEvaluaciones))) &&
    (!filtroEstadoEvaluacion || evaluacion.estado === filtroEstadoEvaluacion)
  );

  async function cargarEmpresas() {
    setError('');
    if (empresas.length > 0) return;
    try {
      setEmpresas(await ordenMeritoService.listarEmpresas());
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudieron cargar las empresas.');
    }
  }

  async function buscar(event: React.FormEvent) {
    event.preventDefault();
    if (!filtros.codigo.trim() && !filtros.nombrePuesto.trim() && !filtros.empresaId) {
      setError('Ingresá al menos un criterio para buscar puestos.');
      return;
    }
    setError(''); setBuscando(true); setBusco(true);
    try {
      const [lista, empresasLista] = await Promise.all([
        ordenMeritoService.buscarPuestos({ codigo: filtros.codigo, nombrePuesto: filtros.nombrePuesto, empresa: filtros.empresaId }),
        ordenMeritoService.listarEmpresas(),
      ]);
      setPuestos(lista); setEmpresas(empresasLista);
    } catch (e) { setPuestos([]); setError(e instanceof Error ? e.message : 'No se pudieron buscar los puestos.'); }
    finally { setBuscando(false); }
  }

  function limpiar() {
    setFiltros({ codigo: '', nombrePuesto: '', empresaId: '' }); setCampoSugerencias(null); setSugerencias([]); setPuestos([]); setBusco(false); setError(''); setPuestoSeleccionado(null); setEvaluaciones([]); setCandidatos([]);
  }

  function seleccionarSugerencia(puesto: PuestoOrdenMerito) {
    setEmpresas((actuales) => actuales.some((empresa) => empresa.id === puesto.empresaId)
      ? actuales
      : [...actuales, { id: puesto.empresaId, nombre: puesto.empresaNombre }]);
    setFiltros((actuales) => ({
      ...actuales,
      codigo: puesto.codigo,
      nombrePuesto: puesto.nombrePuesto,
      empresaId: puesto.empresaId,
    }));
    setCampoSugerencias(null);
  }

  function reiniciarFiltrosEvaluacion() {
    setFiltroNombreEvaluacion('');
    setModoFechaEvaluacion('dia');
    setFechaEvaluacion('');
    setFechaDesdeEvaluaciones('');
    setFechaHastaEvaluaciones('');
    setFiltroEstadoEvaluacion('');
    setFiltrosEvaluacionAbiertos(false);
  }

  function campoPuesto(campo: 'codigo' | 'nombrePuesto', etiqueta: string, placeholder: string) {
    const abierto = campoSugerencias === campo && Boolean(terminoSugerencias);
    const sugerenciasActuales = claveSugerencias === consultaSugerencias ? sugerencias : [];
    const textoActual = filtros[campo];
    const textoBuscado = textoActual.trim();
    const sugerenciaBase = sugerenciasActuales
      .map((puesto) => campo === 'codigo' ? puesto.codigo : puesto.nombrePuesto)
      .find((opcion) => opcion.toLocaleLowerCase().startsWith(textoBuscado.toLocaleLowerCase()) && opcion.length > textoBuscado.length);
    const textoEnMinuscula = textoBuscado === textoBuscado.toLocaleLowerCase()
      && textoBuscado !== textoBuscado.toLocaleUpperCase();
    const textoEnMayuscula = textoBuscado === textoBuscado.toLocaleUpperCase()
      && textoBuscado !== textoBuscado.toLocaleLowerCase();
    const restoBase = sugerenciaBase?.slice(textoBuscado.length) || '';
    const restoSugerido = textoEnMinuscula
      ? restoBase.toLocaleLowerCase()
      : textoEnMayuscula
        ? restoBase.toLocaleUpperCase()
        : restoBase;
    const sugerenciaEnLinea = sugerenciaBase ? `${textoBuscado}${restoSugerido}` : undefined;

    return <label className="space-y-1.5 text-sm font-medium text-[#000000]">{etiqueta}
      <div className="relative" onBlur={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget as Node | null)) setCampoSugerencias(null);
      }}>
        <input
            className={inputClass}
            placeholder={abierto && sugerenciasActuales.length > 0 ? '' : placeholder}
            value={textoActual}
            role="combobox"
            aria-autocomplete="both"
            aria-expanded={abierto && sugerenciasActuales.length > 0}
            aria-controls={`sugerencias-${campo}`}
            aria-describedby={restoSugerido ? `ayuda-autocompletado-${campo}` : undefined}
            onFocus={() => { setError(''); setCampoSugerencias(campo); }}
            onChange={(event) => { setError(''); setFiltros((actuales) => ({ ...actuales, [campo]: event.target.value })); }}
            onKeyDown={(event) => {
              if (event.key === 'Escape') setCampoSugerencias(null);
              if (restoSugerido && (event.key === 'Tab' || (event.key === 'ArrowRight' && event.currentTarget.selectionStart === textoActual.length))) {
                event.preventDefault();
                setFiltros((actuales) => ({ ...actuales, [campo]: sugerenciaEnLinea! }));
              } else if (event.key === 'Enter' && abierto && sugerenciasActuales.length > 0) {
                event.preventDefault();
                seleccionarSugerencia(sugerenciasActuales[0]);
              }
            }}
          />
        {restoSugerido && <span id={`ayuda-autocompletado-${campo}`} className="mt-1 block text-xs font-normal text-[#757575]">Sugerencia: {sugerenciaEnLinea} · Tab o → para completar</span>}
        {abierto && sugerenciasActuales.length > 0 && <ul id={`sugerencias-${campo}`} role="listbox" className="absolute z-20 mt-1 max-h-64 w-full overflow-auto rounded-lg border border-[#D4CCDF] bg-white py-1 shadow-lg">
          {sugerenciasActuales.map((puesto) => <li key={puesto.id} role="option" aria-selected="false">
            <button type="button" className="flex w-full flex-col items-start px-3.5 py-2 text-left normal-case hover:bg-[#F6F3FA] focus:bg-[#F6F3FA] focus:outline-none" onClick={() => seleccionarSugerencia(puesto)}>
              <span className="text-sm font-semibold text-[#24212A]">{puesto.codigo} · {puesto.nombrePuesto}</span>
              <span className="text-xs font-normal text-[#77717E]">{puesto.empresaNombre}</span>
            </button>
          </li>)}
        </ul>}
      </div>
    </label>;
  }

  async function abrirReporte() {
    if (!puestoSeleccionado || evaluacionesSeleccionadas.length === 0) return;
    try {
      const data = await ordenMeritoService.obtenerReporte(puestoSeleccionado.id, evaluacionesSeleccionadas);
      const reporte: Candidato[] = data.map((c) => ({ ...c, documento: `${c.tipoDocumento} ${c.nroDocumento}`, estado: c.estadoMerito === 'APTO' ? 'apto' : c.estadoMerito === 'NO_ALCANZA_MINIMOS' ? 'minimo' : c.estadoMerito === 'PENDIENTE_PUNTAJE' ? 'pendiente' : 'incompleto', puntaje: c.puntajeObtenido, inicio: c.fechaInicio || 'Sin iniciar', fin: c.fechaFinalizacion || undefined }));
      setCandidatos(reporte);
      setError('');
    } catch (e) { setError(e instanceof Error ? e.message : 'No se pudo generar el reporte.'); return; }
    setPuestoReporte(puestoSeleccionado);
    setPuestoSeleccionado(null);
  }

  async function descargarReporte(formato: 'WORD' | 'EXCEL') {
    if (!puestoReporte) return;
    setMenuExportacionAbierto(false);
    setExportando(true);
    setError('');
    try {
      await ordenMeritoService.descargarReporte(puestoReporte.id, evaluacionesSeleccionadas, formato);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo descargar el informe.');
    } finally {
      setExportando(false);
    }
  }

  async function imprimirReporte() {
    if (!puestoReporte) return;
    setExportando(true);
    setError('');
    try {
      await ordenMeritoService.imprimirReporte(puestoReporte.id, evaluacionesSeleccionadas);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo abrir el PDF para imprimir.');
    } finally {
      setExportando(false);
    }
  }

  const reporteVisible = !puestoSeleccionado && evaluacionesSeleccionadas.length > 0;
  const empresaNombre = (puesto: PuestoOrdenMerito) => puesto.empresaNombre || empresas.find((e) => e.id === puesto.empresaId)?.nombre || 'Empresa';
  const candidatosAptos = candidatos.filter((c) => c.estado === 'apto');
  const candidatosIncompletos = candidatos.filter((c) => c.estado === 'incompleto');
  const candidatosSinMinimos = candidatos.filter((c) => c.estado === 'minimo');
  const candidatosPendientes = candidatos.filter((c) => c.estado === 'pendiente');
  const evaluacionesDelReporte = evaluaciones.filter((e) => evaluacionesSeleccionadas.includes(e.id));

  return <div className={`orden-merito-report mx-auto w-full max-w-7xl space-y-6 ${reporteVisible ? 'print:max-w-none print:space-y-3 print:text-black' : ''}`}>
    <header className="border-b-4 border-[#E4E1E8] pb-3 text-center"><h1 className="text-2xl font-bold tracking-wide text-[#27232D] sm:text-3xl">ORDEN DE MÉRITOS</h1></header>
    {mostrarError && error && <div role="alert" className="flex items-start gap-3 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800"><AlertCircle size={18} aria-hidden="true" className="mt-0.5 shrink-0"/><p className="flex-1">{error}</p><button type="button" onClick={() => setError('')} aria-label="Cerrar mensaje de error" title="Cerrar mensaje de error" className="shrink-0 rounded p-1 text-red-700 hover:bg-red-100 focus-visible:outline focus-visible:outline-2 focus-visible:outline-red-700"><X size={16} aria-hidden="true"/></button></div>}

    {reporteVisible ? <>
      <div className="flex flex-wrap items-center justify-between gap-3 print:hidden"><div><button onClick={() => { setPuestoSeleccionado(puestoReporte); }} className="mb-2 inline-flex items-center gap-2 text-sm font-medium text-[#63499A] hover:underline"><ArrowLeft size={16}/> Volver a evaluaciones</button><p className="text-sm text-[#77717E]">{puestoReporte?.nombrePuesto || 'Puesto seleccionado'} · {evaluacionesSeleccionadas.length === evaluaciones.length ? 'Todas las evaluaciones' : `${evaluacionesSeleccionadas.length} evaluaciones`}</p></div><div className="flex items-center gap-2"><Button onClick={imprimirReporte} isLoading={exportando} aria-label="Imprimir orden de mérito"><Printer size={17} aria-hidden="true"/>Imprimir</Button><div className="relative"><button type="button" disabled={exportando} aria-label="Más opciones de descarga" aria-expanded={menuExportacionAbierto} onClick={() => setMenuExportacionAbierto((abierto) => !abierto)} className="rounded-lg border border-[#D4CCDF] bg-white p-2 text-[#51377F] hover:bg-[#F6F3FA] disabled:opacity-60"><MoreHorizontal size={20}/></button>{menuExportacionAbierto && <div className="absolute right-0 z-30 mt-2 w-56 overflow-hidden rounded-lg border border-[#E3DFE9] bg-white py-1 text-sm shadow-lg"><button type="button" onClick={() => descargarReporte('EXCEL')} className="flex w-full items-center gap-3 px-4 py-2.5 text-left hover:bg-[#F6F3FA]"><FileSpreadsheet size={18} aria-hidden="true" className="text-[#28744A]"/><span>Descargar planilla Excel</span></button><button type="button" onClick={() => descargarReporte('WORD')} className="flex w-full items-center gap-3 px-4 py-2.5 text-left hover:bg-[#F6F3FA]"><FileText size={18} aria-hidden="true" className="text-blue-700"/><span>Descargar documento Word</span></button></div>}</div></div></div>
      <div className="hidden text-center print:block"><h1 className="text-2xl font-bold">ORDEN DE MÉRITO</h1><p className="mt-2 text-sm">Puesto: {puestoReporte?.codigo} · {puestoReporte?.nombrePuesto}</p><p className="text-sm">Empresa: {empresaNombre(puestoReporte!)}</p><p className="text-xs">Evaluaciones incluidas: {evaluacionesDelReporte.map((e) => `${e.id} - ${e.proyecto} (${e.fecha})`).join('; ') || 'No hay información disponible'}</p></div>
      <section className={`${panelClass} overflow-hidden print:break-inside-avoid print:border-gray-300 print:shadow-none`}>
        <div className="flex items-center justify-between border-b border-[#E7E4EB] px-5 py-4"><h2 className="flex items-center gap-2 text-lg font-semibold"><Award size={21} className="text-[#6548A0]"/> Candidatos aptos</h2><span className="rounded-full bg-[#E9E0FA] px-3 py-1 text-xs font-semibold text-[#51377F]">Total: {candidatosAptos.length}</span></div>
        <div className="overflow-x-auto print:overflow-visible"><table className="w-full min-w-[680px] text-left text-sm print:min-w-0"><thead className="text-xs text-[#6B6670]"><tr>{['Orden','Tipo y Nro Doc','Apellido','Nombre','Puntaje obtenido'].map(x=><th key={x} className="px-5 py-3 font-semibold">{x}</th>)}</tr></thead><tbody>{candidatosAptos.length ? candidatosAptos.map((c,index)=><tr key={c.cuestionarioId} className="border-t border-[#F0EDF3]"><td className="px-5 py-3.5 font-bold">#{index+1}</td><td className="px-5 py-3.5">{c.documento}</td><td className="px-5 py-3.5 font-semibold">{c.apellido}</td><td className="px-5 py-3.5">{c.nombre}</td><td className="px-5 py-3.5 font-bold">{c.puntaje?.toFixed(2)}</td></tr>) : <tr><td colSpan={5} className="px-5 py-5 text-center text-sm text-[#77717E]">No hay información disponible para esta sección.</td></tr>}</tbody></table></div>
      </section>
      <div className="grid gap-5 lg:grid-cols-2">
        <section className={`${panelClass} p-5`}><h2 className="mb-4 flex items-center gap-2 border-b border-[#E7E4EB] pb-3 text-lg font-semibold"><ClipboardList size={20} className="text-[#77717E]"/> Cuestionarios incompletos</h2><div className="space-y-3">{candidatosIncompletos.length ? candidatosIncompletos.map(c=><CandidatoDetalle key={c.cuestionarioId} candidato={c}/>) : <p className="py-4 text-center text-sm text-[#77717E]">No hay información disponible para esta sección.</p>}</div></section>
        <section className={`${panelClass} p-5`}><h2 className="mb-4 flex items-center gap-2 border-b border-[#E7E4EB] pb-3 text-lg font-semibold"><ArrowRight size={20} className="text-[#77717E]"/> No alcanzan los mínimos requeridos</h2><div className="space-y-3">{candidatosSinMinimos.length ? candidatosSinMinimos.map(c=><CandidatoDetalle key={c.cuestionarioId} candidato={c} />) : <p className="py-4 text-center text-sm text-[#77717E]">No hay información disponible para esta sección.</p>}</div></section>
      </div>
      <section className={`${panelClass} p-5`}><h2 className="mb-3 text-lg font-semibold">Puntaje pendiente</h2><div className="grid gap-3 md:grid-cols-2">{candidatosPendientes.length ? candidatosPendientes.map(c=><CandidatoDetalle key={c.cuestionarioId} candidato={c}/>) : <p className="py-4 text-center text-sm text-[#77717E] md:col-span-2">No hay información disponible para esta sección.</p>}</div></section>
      <p className="text-xs text-[#77717E] print:hidden">Los candidatos se muestran según el estado real de su cuestionario. El puntaje aparecerá cuando el sistema persista las respuestas de evaluación.</p>
    </> : puestoSeleccionado ? <>
      <div><button onClick={() => { setPuestoSeleccionado(null); setEvaluacionesSeleccionadas([]); }} className="mb-2 inline-flex items-center gap-2 text-sm font-medium text-[#63499A] hover:underline"><ArrowLeft size={16}/> Volver a resultados</button><p className="text-sm text-[#77717E]">Puesto: {puestoSeleccionado.nombrePuesto} · {empresaNombre(puestoSeleccionado)}</p><h2 className="mt-1 text-xl font-semibold">Seleccionar evaluaciones</h2></div>
      <section className={`${panelClass} overflow-hidden`}>
        <div className="space-y-3 border-b border-[#E7E4EB] bg-[#FAF9FC] px-4 py-3">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <label className="flex items-center gap-2 text-sm font-semibold">
            <input type="checkbox" className="h-5 w-5 shrink-0 accent-[#6749A3]" checked={evaluaciones.length > 0 && evaluacionesSeleccionadas.length === evaluaciones.length} onChange={(e) => setEvaluacionesSeleccionadas(e.target.checked ? evaluaciones.map((x) => x.id) : [])} />
            Seleccionar todas las evaluaciones
            </label>
            <button type="button" aria-expanded={filtrosEvaluacionAbiertos} aria-controls="filtros-evaluacion" onClick={() => setFiltrosEvaluacionAbiertos((abiertos) => !abiertos)} className="inline-flex items-center gap-2 rounded-lg border border-[#D4CCDF] bg-white px-3 py-2 text-sm font-semibold text-[#51377F] hover:bg-[#F6F3FA]">
              <SlidersHorizontal size={16}/>
              Filtros
              <ChevronDown size={16} className={`transition-transform ${filtrosEvaluacionAbiertos ? 'rotate-180' : ''}`}/>
            </button>
          </div>
          {filtrosEvaluacionAbiertos && <div id="filtros-evaluacion" className="grid gap-3 rounded-lg border border-[#E3DFE9] bg-white p-3 sm:grid-cols-2 lg:grid-cols-3">
            <label className="space-y-1 text-xs font-semibold text-[#57515F]">
              Nombre
              <span className="relative block"><Search size={16} className="absolute left-3 top-2.5 text-[#77717E]"/><input value={filtroNombreEvaluacion} onChange={(e) => setFiltroNombreEvaluacion(e.target.value)} placeholder="Nombre del candidato" className={`${inputClass} py-2 pl-9`} /></span>
            </label>
            <label className="space-y-1 text-xs font-semibold text-[#57515F]">
              Estado
              <select value={filtroEstadoEvaluacion} onChange={(e) => setFiltroEstadoEvaluacion(e.target.value)} className={`${inputClass} py-2`}>
                <option value="">Todos los estados</option>
                <option value="Completada">Completada</option>
                <option value="Parcial">Parcial</option>
              </select>
            </label>
            <fieldset className="space-y-2 sm:col-span-2 lg:col-span-1">
              <legend className="text-xs font-semibold text-[#57515F]">Fecha · dd/mm/aaaa</legend>
              <div className="inline-flex rounded-lg border border-[#D4CCDF] bg-[#F6F3FA] p-0.5" role="group" aria-label="Modo de filtro por fecha">
                <button type="button" onClick={() => setModoFechaEvaluacion('dia')} aria-pressed={modoFechaEvaluacion === 'dia'} className={`rounded-md px-3 py-1 text-xs font-medium ${modoFechaEvaluacion === 'dia' ? 'bg-white text-[#51377F] shadow-sm' : 'text-[#77717E]'}`}>Día</button>
                <button type="button" onClick={() => setModoFechaEvaluacion('rango')} aria-pressed={modoFechaEvaluacion === 'rango'} className={`rounded-md px-3 py-1 text-xs font-medium ${modoFechaEvaluacion === 'rango' ? 'bg-white text-[#51377F] shadow-sm' : 'text-[#77717E]'}`}>Rango</button>
              </div>
              {modoFechaEvaluacion === 'dia' ? <input aria-label="Elegir fecha" type="date" lang="es-AR" max={fechaMaxima} value={fechaEvaluacion} onChange={(e) => setFechaEvaluacion(e.target.value)} className={`${inputClass} min-w-0 px-2 py-2 text-xs`} /> : <div className="grid grid-cols-2 gap-2">
                <label className="space-y-1 text-[11px] text-[#77717E]">Desde
                  <input aria-label="Fecha desde" type="date" lang="es-AR" max={fechaMaxima} value={fechaDesdeEvaluaciones} onChange={(e) => {
                    const desde = e.target.value;
                    setFechaDesdeEvaluaciones(desde);
                    if (desde && fechaHastaEvaluaciones && fechaHastaEvaluaciones < desde) setFechaHastaEvaluaciones(desde);
                  }} className={`${inputClass} min-w-0 px-2 py-2 text-xs`} />
                </label>
                <label className="space-y-1 text-[11px] text-[#77717E]">Hasta
                  <input aria-label="Fecha hasta" type="date" lang="es-AR" max={fechaMaxima} min={fechaDesdeEvaluaciones || undefined} value={fechaHastaEvaluaciones} onChange={(e) => setFechaHastaEvaluaciones(e.target.value)} className={`${inputClass} min-w-0 px-2 py-2 text-xs`} />
                </label>
              </div>}
              <p className="text-[11px] font-normal text-[#77717E]">El calendario permite elegir desde hoy hacia fechas anteriores.</p>
            </fieldset>
          </div>}
        </div>
        {evaluaciones.length ? evaluacionesFiltradas.length ? <div className="divide-y divide-[#ECE9EF]">{evaluacionesFiltradas.map((e, index) => <label key={e.id} className="grid cursor-pointer grid-cols-[28px_1fr_100px] items-center gap-3 px-4 py-4 hover:bg-[#FAF9FC] sm:grid-cols-[36px_1fr_130px_1fr_110px]"><input type="checkbox" className="h-5 w-5 shrink-0 accent-[#6749A3]" checked={evaluacionesSeleccionadas.includes(e.id)} onChange={(ev) => setEvaluacionesSeleccionadas((prev) => ev.target.checked ? [...prev, e.id] : prev.filter((id) => id !== e.id))}/><span className="font-semibold">{index + 1}<small className="block font-normal text-[#77717E] sm:hidden">{e.fecha}</small></span><span className="hidden text-sm text-[#625D67] sm:block">{e.fecha}</span><span className="hidden text-sm text-[#625D67] sm:block">{e.proyecto}</span><span className={`rounded-full px-2 py-1 text-center text-xs ${e.estado === 'Completada' ? 'bg-[#E4F6EA] text-[#257348]' : 'bg-[#FFF2D6] text-[#946000]'}`}>{e.estado}</span></label>)}</div> : <p className="px-5 py-10 text-center text-sm text-[#77717E]">No hay evaluaciones que coincidan con los filtros ingresados.</p> : <p className="px-5 py-10 text-center text-sm text-[#77717E]">Este puesto todavía no tiene cuestionarios asociados.</p>}
        <div className="flex flex-wrap items-center justify-between gap-3 bg-[#FAF9FC] px-4 py-3"><span className="text-sm text-[#625D67]">{evaluacionesSeleccionadas.length} evaluaciones seleccionadas</span><div className="flex gap-2"><Button variant="secondary" onClick={()=>{setPuestoSeleccionado(null);setEvaluacionesSeleccionadas([])}}>Cancelar</Button><Button onClick={abrirReporte} disabled={!evaluacionesSeleccionadas.length} rightIcon="arrow_forward">Generar orden</Button></div></div>
      </section>
    </> : <>
      <form onSubmit={buscar} className={`${panelClass} p-5 sm:p-6`}><h2 className="mb-4 text-lg font-semibold">Buscar puestos</h2><div className="grid gap-4 md:grid-cols-3">{campoPuesto('codigo', 'Código del puesto', 'Ej. PUE-2026-101')}{campoPuesto('nombrePuesto', 'Nombre del puesto', 'Ej. Gerente, Analista...')}<label className="space-y-1.5 text-sm font-medium text-[#000000]">Empresa<span className="relative block"><select className={`${inputClass} appearance-none pr-9`} value={filtros.empresaId} onFocus={async()=>{setError('');if(!empresas.length)setEmpresas(await ordenMeritoService.listarEmpresas())}} onChange={e=>{setError('');setFiltros({...filtros,empresaId:e.target.value})}}><option value="">Seleccione una empresa</option>{empresas.map(e=><option key={e.id} value={e.id}>{e.nombre}</option>)}</select><ChevronDown size={16} className="pointer-events-none absolute right-3 top-3.5 text-[#77717E]"/></span></label></div><div className="mt-5 flex justify-end gap-2"><Button type="button" variant="outline" onClick={limpiar}>Limpiar</Button><Button type="submit" isLoading={buscando} leftIcon="search">Buscar</Button></div></form>
      {busco ? <><div className="flex items-end justify-between"><div><p className="text-xs font-semibold uppercase tracking-wider text-[#77717E]">Módulo de convocatorias</p><h2 className="mt-1 text-xl font-semibold">Resultados de la búsqueda</h2><p className="mt-1 text-sm text-[#77717E]">{resultados.length} puestos encontrados</p></div></div>{resultados.length ? <div className="grid gap-4 md:grid-cols-2">{resultados.map((puesto)=><article key={puesto.id} className={`${panelClass} flex flex-col p-5`}><div className="flex items-center justify-between gap-3"><span className="rounded bg-[#F0EFF2] px-2.5 py-1 text-xs tracking-wider text-[#726C78]">{puesto.codigo}</span><span className="flex items-center gap-1.5 rounded-full bg-[#F6F5F8] px-2.5 py-1 text-xs text-[#625D67]"><Building2 size={13}/>{empresaNombre(puesto)}</span></div><h3 className="mt-4 text-lg font-semibold">{puesto.nombrePuesto}</h3><p className="mt-1 line-clamp-2 min-h-10 text-sm text-[#625D67]">{puesto.descripcion || 'Puesto disponible para evaluación de candidatos.'}</p><div className="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-[#E7E4EB] pt-4"><div><p className="text-xs font-semibold uppercase text-[#625D67]">Evaluaciones completas</p><p className="mt-1 flex items-center gap-2"><CheckCircle2 size={16} className="text-[#5F5970]"/><strong>{String(puesto.evaluacionesCompletas).padStart(2,'0')}</strong><span className="text-xs text-[#625D67]">de {puesto.candidatos} cuestionarios</span></p></div><Button size="sm" onClick={async()=>{setError('');try{const lista=await ordenMeritoService.listarEvaluaciones(puesto.id);if(!lista.length){setError('Este puesto no posee evaluaciones asociadas.');return}reiniciarFiltrosEvaluacion();setEvaluaciones(lista);setPuestoSeleccionado(puesto);setEvaluacionesSeleccionadas([])}catch(e){setError(e instanceof Error?e.message:'No se pudieron cargar las evaluaciones.')}}} rightIcon="arrow_forward">Emitir orden de mérito</Button></div></article>)}</div> : <div className={`${panelClass} px-6 py-14 text-center`}><Search size={28} className="mx-auto mb-3 text-[#8872B4]"/><h3 className="font-semibold">No se encontraron puestos</h3><p className="mt-1 text-sm text-[#77717E]">Probá con otros criterios de búsqueda.</p></div>}</> : <div className={`${panelClass} px-6 py-12 text-center`}><div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-[#EEE6FA] text-[#684DA0]"><Search size={24}/></div><h2 className="font-semibold">Buscá un puesto para emitir su orden de mérito</h2><p className="mx-auto mt-2 max-w-lg text-sm text-[#77717E]">Ingresá código, nombre o empresa. Luego podrás elegir una o todas las evaluaciones disponibles.</p></div>}

    </>}
  </div>;
}

function CandidatoDetalle({ candidato }: { candidato: Candidato }) {
  return <article className="overflow-hidden rounded-lg border border-[#E2DFE6] bg-white">
    <header className="flex flex-wrap items-center justify-between gap-3 bg-[#F8F8FA] px-4 py-3">
      <div><p className="font-semibold text-[#29252E]">{candidato.apellido}, {candidato.nombre}</p><p className="mt-0.5 text-xs text-[#6E6973]">{candidato.documento}</p></div>
      <span className="rounded-full bg-[#EEEAF4] px-3 py-1 text-xs font-semibold text-[#51377F]">{candidato.estadoCuestionario.replaceAll('_', ' ')}</span>
    </header>
    <div className="grid gap-3 border-b border-[#E7E4EB] px-4 py-3 text-xs sm:grid-cols-3">
      <p><span className="block text-[#77717E]">Fecha inicio</span><strong className="mt-0.5 block text-[#29252E]">{candidato.inicio}</strong></p>
      <p><span className="block text-[#77717E]">Fecha finalización</span><strong className="mt-0.5 block text-[#29252E]">{candidato.fin || 'Sin completar'}</strong></p>
      <p><span className="block text-[#77717E]">Puntaje total</span><strong className="mt-0.5 block text-[#29252E]">{candidato.puntaje === null ? 'Pendiente' : candidato.puntaje.toFixed(2)}</strong></p>
    </div>
    {candidato.estado === 'minimo' && <section className="p-4">
      <h3 className="mb-2 text-sm font-semibold text-[#29252E]">Competencias por debajo del mínimo</h3>
      <div className="overflow-x-auto rounded-md border border-[#E7E4EB]">
        <table className="w-full min-w-[420px] text-left text-xs">
          <thead className="bg-[#F6F3FA] text-[#57515F]"><tr><th className="px-3 py-2 font-semibold">Competencia</th><th className="px-3 py-2 text-right font-semibold">Puntaje obtenido</th><th className="px-3 py-2 text-right font-semibold">Mínimo requerido</th></tr></thead>
          <tbody className="divide-y divide-[#ECE9EF]">{candidato.minimosNoAlcanzados.map((item) => <tr key={item.competencia}><td className="px-3 py-2.5 font-medium text-[#29252E]">{item.competencia}</td><td className="px-3 py-2.5 text-right text-[#9B3030]">{item.puntajeObtenido.toFixed(2)}</td><td className="px-3 py-2.5 text-right text-[#625D67]">{item.puntajeMinimo.toFixed(2)}</td></tr>)}</tbody>
        </table>
      </div>
    </section>}
    {candidato.datosAdicionales && <p className="border-t border-[#E7E4EB] px-4 py-3 text-xs text-[#625D67]"><strong className="text-[#29252E]">Datos adicionales: </strong>{candidato.datosAdicionales}</p>}
  </article>;
}

export default function OrdenMeritosPage() { return <DashboardLayout><OrdenMeritosContent /></DashboardLayout>; }
