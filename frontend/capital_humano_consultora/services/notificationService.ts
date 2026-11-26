import { User } from '@/types/auth';

export interface AppNotification {
  id: string;
  title: string;
  message: string;
  createdAt: string;
  isRead: boolean;
}

type NotificationOwner = Pick<User, 'id' | 'name'>;

const STORAGE_PREFIX = 'app_notifications:';

function storageKey(userId: string) {
  return `${STORAGE_PREFIX}${userId}`;
}

function read(userId: string): AppNotification[] {
  if (typeof window === 'undefined') return [];
  try {
    const stored = window.localStorage.getItem(storageKey(userId));
    if (!stored) return [];
    const parsed: unknown = JSON.parse(stored);
    return Array.isArray(parsed) ? parsed as AppNotification[] : [];
  } catch {
    return [];
  }
}

function save(userId: string, notifications: AppNotification[]) {
  if (typeof window !== 'undefined') {
    window.localStorage.setItem(storageKey(userId), JSON.stringify(notifications));
  }
}

export const notificationService = {
  initializeForUser(user: NotificationOwner): AppNotification[] {
    if (typeof window === 'undefined') return [];
    const key = storageKey(user.id);
    const existing = window.localStorage.getItem(key);
    if (existing !== null) return read(user.id);

    const welcome: AppNotification = {
      id: `welcome-${user.id}`,
      title: '¡Te damos la bienvenida!',
      message: `Hola, ${user.name}. Tu cuenta está lista para usar el sistema. Aquí vas a recibir novedades y avisos importantes.`,
      createdAt: new Date().toISOString(),
      isRead: false,
    };
    save(user.id, [welcome]);
    return [welcome];
  },

  markAsRead(userId: string, notificationId: string): AppNotification[] {
    const updated = read(userId).map((notification) => notification.id === notificationId
      ? { ...notification, isRead: true }
      : notification);
    save(userId, updated);
    return updated;
  },
};
