'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useSidebar } from '@/context/SidebarContext';
import { UserProfileBadge } from '@/components/molecules/UserProfileBadge';
import { Icon } from '@/components/ui/Icon';
import { useRouter } from 'next/navigation';
import { ROUTES } from '@/constants/routes';
import { User } from '@/types/auth';
import { AppNotification, notificationService } from '@/services/notificationService';

export interface TopbarProps {
  consultorNombre?: string;
  className?: string;
}

const DEFAULT_MOCK_CONSULTOR: User = {
  id: 'usr-default-consultor',
  name: 'LIC. MARTÍN GÓMEZ',
  username: 'mgomez',
  email: 'martin.gomez@consultora.com',
  role: 'CONSULTOR',
};

export const Topbar: React.FC<TopbarProps> = ({ consultorNombre, className = '' }) => {
  const { user, logout } = useAuth();
  const { toggle } = useSidebar();
  const router = useRouter();

  const handleLogout = () => {
    logout();
    router.replace(ROUTES.AUTH.LOGIN);
  };

  const isCandidato = user?.role === 'CANDIDATO';

  // Resolver usuario a mostrar: parámetro explícito > usuario autenticado > mockup por defecto
  const displayUser: User = consultorNombre
    ? {
        id: 'usr-custom',
        name: consultorNombre,
        username: consultorNombre.toLowerCase().replace(/\s+/g, '.'),
        email: `${consultorNombre.toLowerCase().replace(/\s+/g, '.')}@consultora.com`,
        role: 'CONSULTOR',
      }
    : user || DEFAULT_MOCK_CONSULTOR;
  const notificationUserId = user?.id || (consultorNombre ? displayUser.id : null);
  const notificationUserName = user?.name || (consultorNombre ? displayUser.name : null);
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [openedNotificationId, setOpenedNotificationId] = useState<string | null>(null);
  const [logoutConfirmationOpen, setLogoutConfirmationOpen] = useState(false);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      if (notificationUserId && notificationUserName) {
        setNotifications(notificationService.initializeForUser({ id: notificationUserId, name: notificationUserName }));
      } else {
        setNotifications([]);
      }
    }, 0);
    return () => window.clearTimeout(timer);
  }, [notificationUserId, notificationUserName]);

  const abrirNotificacion = (notification: AppNotification) => {
    if (!notificationUserId) return;
    setOpenedNotificationId((current) => current === notification.id ? null : notification.id);
    if (!notification.isRead) {
      setNotifications(notificationService.markAsRead(notificationUserId, notification.id));
    }
  };

  const marcarComoLeida = (notificationId: string) => {
    if (!notificationUserId) return;
    setNotifications(notificationService.markAsRead(notificationUserId, notificationId));
  };

  const formatoFechaNotificacion = (createdAt: string) => new Intl.DateTimeFormat('es-AR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(createdAt));

  const confirmarSalida = () => {
    setLogoutConfirmationOpen(false);
    handleLogout();
  };

  return (
    <header
      className={`bg-[#3B3B3B] text-white h-20 px-4 md:px-8 lg:px-12 flex items-center justify-between shadow-md select-none sticky top-0 z-20 w-full ${className}`}
    >
      {/* Sección Izquierda: Menú Hamburguesa + Marca */}
      <div className="flex items-center gap-4 md:gap-6">
        {!isCandidato && (
          <button
            type="button"
            onClick={toggle}
            className="p-2 rounded-lg hover:bg-white/10 active:bg-white/20 transition-colors text-white cursor-pointer"
            aria-label="Abrir o cerrar menú lateral"
          >
            <Icon name="menu" size={28} />
          </button>
        )}

        <div className="flex items-center gap-3">
          <div className="text-white flex items-center">
            <Icon name="groups" size={32} />
          </div>
          <span className="font-semibold text-base md:text-xl tracking-wider text-white">
            CONSULTORA CAPIT@L HUMANO
          </span>
        </div>
      </div>

      {/* Sección Derecha: Usuario Consultor o Salir para Candidato */}
      <div className="flex items-center gap-3 md:gap-6">
        {!isCandidato && <UserProfileBadge user={displayUser} />}

        <div className="flex items-center gap-2">
          <div className="relative">
            <button
              type="button"
              onClick={() => setNotificationsOpen((open) => !open)}
              className="relative cursor-pointer rounded-lg border border-white/20 bg-transparent p-2 text-white transition-colors hover:bg-white/10"
              aria-label="Notificaciones"
              aria-expanded={notificationsOpen}
              aria-controls="panel-notificaciones"
              title="Notificaciones"
            >
              <Icon name="notifications" size={20} />
              {notifications.length > 0 && <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-[#D32F2F]" />}
            </button>

            {notificationsOpen && <section id="panel-notificaciones" aria-label="Panel de notificaciones" className="absolute right-0 z-50 mt-3 w-[min(90vw,24rem)] overflow-hidden rounded-xl border border-[#E3DFE9] bg-white text-[#24212A] shadow-xl">
              <header className="border-b border-[#E7E4EB] bg-[#FAF9FC] px-5 py-4 text-center">
                <h2 className="text-base font-semibold">Notificaciones</h2>
                {notifications.length > 0 && <p className="mt-1 text-xs text-[#77717E]">{notifications.length} {notifications.length === 1 ? 'mensaje' : 'mensajes'}</p>}
              </header>
              {notifications.length === 0 ? <div className="px-5 py-12 text-center text-sm text-[#77717E]">No se encuentran notificaciones</div> : <ul className="max-h-[min(60vh,24rem)] space-y-2 overflow-y-auto p-3 text-center">
                {notifications.map((notification) => {
                  const isOpen = openedNotificationId === notification.id;
                  return <li key={notification.id} className={`rounded-lg border px-4 py-3 transition-colors ${notification.isRead ? 'border-[#D4D0D9] bg-[#E8E5EB]' : 'border-[#E3D9F4] bg-[#F7F3FC]'}`}>
                    <button type="button" onClick={() => abrirNotificacion(notification)} aria-expanded={isOpen} className="w-full text-center">
                      <span className="block text-sm font-semibold">{notification.title}</span>
                      <span className="mt-1 block text-xs text-[#77717E]">{formatoFechaNotificacion(notification.createdAt)}</span>
                      {isOpen && <span className="mt-3 block text-sm leading-relaxed text-[#4A4650]">{notification.message}</span>}
                    </button>
                    {!notification.isRead && <button type="button" onClick={() => marcarComoLeida(notification.id)} className="mt-3 text-xs font-semibold text-[#51377F] underline-offset-2 hover:underline">Marcar como leída</button>}
                  </li>;
                })}
              </ul>}
            </section>}
          </div>

          <button
            type="button"
            onClick={() => setLogoutConfirmationOpen(true)}
            className={`${isCandidato ? 'flex items-center gap-2 px-3.5 py-1.5 text-sm font-semibold' : 'p-2'} cursor-pointer rounded-lg border border-white/20 bg-transparent text-white transition-colors hover:bg-white/10`}
            aria-label="Cerrar sesión"
            title="Cerrar sesión"
          >
            <Icon name="logout" size={20} />
            {isCandidato && <span>Salir</span>}
          </button>
        </div>
      </div>
      {logoutConfirmationOpen && <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50 px-4">
        <section role="dialog" aria-modal="true" aria-labelledby="confirmar-salida-titulo" className="w-full max-w-sm rounded-2xl bg-white px-6 py-7 text-center text-[#24212A] shadow-2xl">
          <h2 id="confirmar-salida-titulo" className="text-lg font-semibold">¿Querés salir del sistema?</h2>
          <p className="mt-2 text-sm text-[#6E6973]">Se cerrará tu sesión actual.</p>
          <div className="mt-6 flex justify-center gap-3">
            <button type="button" onClick={() => setLogoutConfirmationOpen(false)} className="min-w-24 rounded-lg border border-[#D4CCDF] px-5 py-2 text-sm font-semibold text-[#51377F] hover:bg-[#F6F3FA]">No</button>
            <button type="button" onClick={confirmarSalida} className="min-w-24 rounded-lg bg-[#51377F] px-5 py-2 text-sm font-semibold text-white hover:bg-[#3F2B68]">Sí</button>
          </div>
        </section>
      </div>}
    </header>
  );
};

export default Topbar;
