'use client';

import React from 'react';
import Link from 'next/link';
import { Icon, IconName } from '@/components/ui/Icon';
import { ROUTES } from '@/constants/routes';
import { useAuth } from '@/context/AuthContext';

interface DashboardModuleCard {
  title: string;
  description: string;
  href: string;
  icon: IconName;
}

const PREFIJOS_TITULO = new Set(['lic', 'ing', 'dr', 'dra', 'sr', 'sra', 'prof', 'mg', 'esp']);

const MASCULINOS_TERMINADOS_EN_A = new Set([
  'bautista',
  'luca',
  'joshua',
  'borja',
  'ezra',
  'nicola',
  'nikola',
]);

const FEMENINOS_SIN_TERMINACION_A = new Set([
  'abril', 'ailen', 'alice', 'amparo', 'anabel', 'angeles', 'araceli', 'astrid', 'ayelen',
  'beatriz', 'belen', 'betty', 'cande', 'carmen', 'caro', 'celeste', 'celine', 'chloe', 'cindy',
  'claribel', 'cloe', 'consuelo', 'cristal', 'delfi', 'denise', 'dolores', 'edith', 'emily',
  'ester', 'esther', 'flor', 'gaby', 'gisel', 'gisele', 'giselle', 'guada', 'guadalupe',
  'ines', 'ingrid', 'irene', 'iris', 'isabel', 'ivonne', 'jazmin', 'jennifer', 'jenny', 'judith',
  'karen', 'kathy', 'kelly', 'lau', 'lauren', 'lilian', 'lily', 'liz', 'lourdes', 'lucy', 'luli',
  'luz', 'mabel', 'magali', 'mailen', 'maite', 'majo', 'mar', 'marian', 'maribel', 'maricel',
  'marisol', 'mary', 'melanie', 'mercedes', 'michelle', 'milagros', 'mili', 'miriam', 'montserrat',
  'myriam', 'nancy', 'naomi', 'nataly', 'naty', 'nicole', 'nieves', 'noemi', 'pau', 'paz', 'pilar',
  'raquel', 'rochi', 'rocio', 'romi', 'rosario', 'ruth', 'sol', 'sole', 'stephanie', 'vale',
  'valerie', 'vero', 'vicky', 'vivian', 'wendy', 'yael', 'yanet', 'yvonne', 'zoe',
]);

function normalizarTexto(texto: string): string {
  return texto
    .trim()
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');
}

function obtenerSaludoConsultor(nombreCompleto?: string): string {
  if (!nombreCompleto?.trim()) {
    return '¡Bienvenido!';
  }

  const palabras = nombreCompleto.trim().split(/\s+/).filter(Boolean);
  let prefijo: string | null = null;
  let indiceNombre = 0;

  while (indiceNombre < palabras.length - 1) {
    const candidata = normalizarTexto(palabras[indiceNombre]).replace(/\.$/, '');
    if (PREFIJOS_TITULO.has(candidata)) {
      prefijo = candidata;
      indiceNombre += 1;
    } else {
      break;
    }
  }

  const primerNombre = palabras[indiceNombre];
  if (!primerNombre) {
    return '¡Bienvenido!';
  }

  const nombreFormateado =
    primerNombre.charAt(0).toUpperCase() + primerNombre.slice(1).toLowerCase();
  const nombreNormalizado = normalizarTexto(primerNombre);

  let esFemenino: boolean;
  if (prefijo === 'dra' || prefijo === 'sra') {
    esFemenino = true;
  } else if (prefijo === 'dr' || prefijo === 'sr') {
    esFemenino = false;
  } else if (MASCULINOS_TERMINADOS_EN_A.has(nombreNormalizado)) {
    esFemenino = false;
  } else if (FEMENINOS_SIN_TERMINACION_A.has(nombreNormalizado)) {
    esFemenino = true;
  } else {
    esFemenino = nombreNormalizado.endsWith('a');
  }

  return `¡${esFemenino ? 'Bienvenida' : 'Bienvenido'}, ${nombreFormateado}!`;
}

const OPERACION_PRINCIPAL: DashboardModuleCard[] = [
  {
    title: 'Puestos',
    description: 'Administración de perfiles y puestos laborales disponibles.',
    href: ROUTES.CONSULTOR.PUESTOS_NUEVO,
    icon: 'work',
  },
  {
    title: 'Evaluaciones',
    description: 'Gestión y seguimiento de procesos de evaluación activos.',
    href: ROUTES.CONSULTOR.EVALUACIONES,
    icon: 'groups',
  },
  {
    title: 'Orden de Méritos',
    description: 'Consulta de rankings y resultados finales de candidatos.',
    href: ROUTES.CONSULTOR.ORDEN_MERITOS,
    icon: 'format_list_numbered',
  },
];

const CONFIGURACION_EVALUACION: DashboardModuleCard[] = [
  {
    title: 'Competencias',
    description: 'Definición de competencias evaluables del sistema.',
    href: ROUTES.CONSULTOR.COMPETENCIAS,
    icon: 'check_square',
  },
  {
    title: 'Factores',
    description: 'Configuración de factores asociados a cada competencia.',
    href: ROUTES.CONSULTOR.FACTORES_NUEVO,
    icon: 'puzzle',
  },
  {
    title: 'Preguntas',
    description: 'Banco de preguntas para los cuestionarios.',
    href: ROUTES.CONSULTOR.PREGUNTAS,
    icon: 'file_question',
  },
  {
    title: 'Opciones de respuesta',
    description: 'Escalas y alternativas de respuesta disponibles.',
    href: ROUTES.CONSULTOR.OPCIONES_RESPUESTA,
    icon: 'assignment',
  },
];

export const InicioConsultorScreen: React.FC = () => {
  const { user } = useAuth();
  const saludo = obtenerSaludoConsultor(user?.name);

  return (
    <div className="flex flex-col gap-6 w-full max-w-6xl mx-auto pb-12">
      {/* Breadcrumb */}
      <nav
        aria-label="Migas de pan"
        className="flex flex-wrap items-center gap-2 text-sm text-[#757575] font-medium"
      >
        <span className="font-semibold text-[#1E1B4B]">Panel de Gestión Consultor</span>
      </nav>

      {/* Contenedor principal de bienvenida */}
      <section className="bg-white rounded-2xl md:rounded-3xl border border-[#D0BCFF] shadow-xs p-6 sm:p-8 md:p-10">
        <div className="flex flex-col gap-2">
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-[#1E1B4B]">
            {saludo}
          </h1>
          <p className="text-sm sm:text-base text-[#5E5A68] max-w-2xl leading-relaxed">
            Seleccioná un módulo para comenzar a operar o configurar las evaluaciones.
          </p>
        </div>
      </section>

      {/* Sección 1: Operación Principal */}
      <section aria-labelledby="seccion-operacion-principal" className="flex flex-col gap-4">
        <div className="flex items-center gap-3">
          <h2
            id="seccion-operacion-principal"
            className="text-base sm:text-lg font-bold tracking-tight text-[#1E1B4B]"
          >
            Operación Principal
          </h2>
          <div className="h-px flex-1 bg-[#D0BCFF]/60" />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {OPERACION_PRINCIPAL.map((item) => (
            <Link
              key={item.title}
              href={item.href}
              className="group relative flex flex-col justify-between gap-5 rounded-2xl bg-white border border-[#D0BCFF]/80 p-6 shadow-xs cursor-pointer transition-all duration-200 ease-out hover:-translate-y-0.5 hover:border-[#3F2581] hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#3F2581] focus-visible:ring-offset-2"
            >
              <div className="flex items-start justify-between gap-4">
                <div className="w-12 h-12 rounded-xl bg-[#F4F0FF] border border-[#EADDFF] text-[#3F2581] flex items-center justify-center shrink-0 transition-colors duration-200 group-hover:bg-[#3F2581] group-hover:border-[#3F2581] group-hover:text-white">
                  <Icon name={item.icon} size={24} />
                </div>
                <span
                  aria-hidden="true"
                  className="mt-1 text-[#9E8AC8] transition-all duration-200 group-hover:text-[#3F2581] group-hover:translate-x-1"
                >
                  <Icon name="arrow_forward" size={18} />
                </span>
              </div>

              <div className="flex flex-col gap-1.5">
                <h3 className="text-lg font-bold text-[#1E1B4B] transition-colors duration-200 group-hover:text-[#3F2581]">
                  {item.title}
                </h3>
                <p className="text-sm text-[#5E5A68] leading-relaxed">
                  {item.description}
                </p>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Sección 2: Configuración de Evaluación */}
      <section aria-labelledby="seccion-configuracion-evaluacion" className="flex flex-col gap-4 pt-1">
        <div className="flex items-center gap-3">
          <h2
            id="seccion-configuracion-evaluacion"
            className="text-base sm:text-lg font-bold tracking-tight text-[#1E1B4B]"
          >
            Configuración de Evaluación
          </h2>
          <div className="h-px flex-1 bg-[#D0BCFF]/60" />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {CONFIGURACION_EVALUACION.map((item) => (
            <Link
              key={item.title}
              href={item.href}
              className="group relative flex flex-col justify-between gap-4 rounded-2xl bg-white border border-[#D0BCFF]/80 p-5 shadow-xs cursor-pointer transition-all duration-200 ease-out hover:-translate-y-0.5 hover:border-[#3F2581] hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#3F2581] focus-visible:ring-offset-2"
            >
              <div className="flex items-start justify-between gap-3">
                <div className="w-10 h-10 rounded-xl bg-[#F4F0FF] border border-[#EADDFF] text-[#3F2581] flex items-center justify-center shrink-0 transition-colors duration-200 group-hover:bg-[#3F2581] group-hover:border-[#3F2581] group-hover:text-white">
                  <Icon name={item.icon} size={20} />
                </div>
                <span
                  aria-hidden="true"
                  className="mt-1 text-[#9E8AC8] transition-all duration-200 group-hover:text-[#3F2581] group-hover:translate-x-1"
                >
                  <Icon name="arrow_forward" size={16} />
                </span>
              </div>

              <div className="flex flex-col gap-1">
                <h3 className="text-base font-bold text-[#1E1B4B] transition-colors duration-200 group-hover:text-[#3F2581]">
                  {item.title}
                </h3>
                <p className="text-xs sm:text-sm text-[#5E5A68] leading-relaxed">
                  {item.description}
                </p>
              </div>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
};

export default InicioConsultorScreen;
