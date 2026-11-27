export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export class ApiError extends Error {
  constructor(public status: number, message: string, public data?: unknown) {
    super(message);
    this.name = 'ApiError';
  }
}

interface RequestOptions extends RequestInit {
  params?: Record<string, string | number | boolean | undefined>;
}

export async function apiFetch<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
  const { params, headers, ...rest } = options;
  
  let url = `${API_BASE_URL}${endpoint}`;
  if (params) {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== '') {
        searchParams.append(key, String(value));
      }
    });
    const queryString = searchParams.toString();
    if (queryString) {
      url += `?${queryString}`;
    }
  }

  // Obtenemos token de autenticación del storage si estamos en el cliente
  let token: string | null = null;
  if (typeof window !== 'undefined') {
    token = localStorage.getItem('auth_token');
  }

  const defaultHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
  };

  if (token) {
    defaultHeaders['Authorization'] = `Bearer ${token}`;
  }

  let response: Response;
  try {
    response = await fetch(url, {
      ...rest,
      headers: {
        ...defaultHeaders,
        ...(headers as Record<string, string>),
      },
    });

  } catch (error) {
    const message = error instanceof Error ? error.message : 'Error de conexión con el servidor';
    throw new ApiError(500, message || 'Error de conexión con el servidor');
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => null);
    throw new ApiError(
      response.status,
      errorData?.mensaje || errorData?.message || `Error en la solicitud (HTTP ${response.status}${response.statusText ? `: ${response.statusText}` : ''}).`,
      errorData
    );
  }

  if (response.status === 204) {
    return {} as T;
  }

  try {
    return await response.json();
  } catch (error) {
    const message = error instanceof Error ? error.message : 'No se pudo interpretar la respuesta del servidor';
    throw new ApiError(500, message || 'No se pudo interpretar la respuesta del servidor');
  }
}
