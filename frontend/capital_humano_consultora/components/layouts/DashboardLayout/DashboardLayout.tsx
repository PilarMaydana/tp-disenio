'use client';

import React, { useEffect, useRef } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { Topbar } from '@/components/organisms/Topbar';
import { Sidebar } from '@/components/organisms/Sidebar';
import { SidebarProvider } from '@/context/SidebarContext';
import { useAuth } from '@/context/AuthContext';
import { ROUTES } from '@/constants/routes';

export interface DashboardLayoutProps {
  children: React.ReactNode;
  consultorNombre?: string;
  className?: string;
}

function DashboardContent({ children, consultorNombre, className = '' }: DashboardLayoutProps) {
  const { user, isAuthenticated, isLoading } = useAuth();
  const router = useRouter();
  const pathname = usePathname();
  const mainRef = useRef<HTMLElement>(null);

  const isCandidato = user?.role === 'CANDIDATO';
  const isCandidatoRoute = pathname?.startsWith('/candidato') || pathname?.includes('/candidato');

  useEffect(() => {
    mainRef.current?.scrollTo({ top: 0 });
  }, [pathname]);

  useEffect(() => {
    if (isLoading) return;

    // 1. Si no está autenticado, redirigir inmediatamente al login
    if (!isAuthenticated || !user) {
      router.replace(ROUTES.AUTH.LOGIN);
      return;
    }

    // 2. Si es CANDIDATO y está intentando acceder a cualquier ruta que NO sea de candidato
    if (user.role === 'CANDIDATO' && !isCandidatoRoute) {
      router.replace(ROUTES.CANDIDATO.CUESTIONARIO);
      return;
    }

    // 3. Si es CONSULTOR y está intentando acceder a la ruta de cuestionario de candidato
    if (user.role === 'CONSULTOR' && isCandidatoRoute) {
      router.replace(ROUTES.CONSULTOR.INICIO);
      return;
    }
  }, [user, isAuthenticated, isLoading, pathname, isCandidatoRoute, router]);

  // Si está cargando o no autenticado, mostrar loader y no renderizar contenido protegido
  if (isLoading || !isAuthenticated || !user) {
    return (
      <div className="h-screen bg-[#F6F6F6] flex items-center justify-center p-4">
        <div className="w-8 h-8 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  // Prevenir parpadeo de contenido si el rol no coincide con la ruta mientras se ejecuta la redirección
  if (user.role === 'CANDIDATO' && !isCandidatoRoute) {
    return (
      <div className="h-screen bg-[#F6F6F6] flex items-center justify-center p-4">
        <div className="w-8 h-8 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (user.role === 'CONSULTOR' && isCandidatoRoute) {
    return (
      <div className="h-screen bg-[#F6F6F6] flex items-center justify-center p-4">
        <div className="w-8 h-8 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="h-screen w-full flex flex-col overflow-hidden bg-[#F6F6F6] print:h-auto print:overflow-visible">
      {!isCandidato && <Sidebar />}
      <Topbar consultorNombre={consultorNombre} />
      <main
        ref={mainRef}
        id="dashboard-main-scroll"
        className={`flex-1 min-h-0 w-full overflow-y-auto px-4 sm:px-8 lg:px-12 py-6 sm:py-8 print:overflow-visible ${className}`}
      >
        {children}
      </main>
    </div>
  );
}

export const DashboardLayout: React.FC<DashboardLayoutProps> = (props) => {
  return (
    <SidebarProvider>
      <DashboardContent {...props} />
    </SidebarProvider>
  );
};

export default DashboardLayout;
