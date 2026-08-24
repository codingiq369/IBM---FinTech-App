/**
 * The one place that knows how to talk HTTP to the backend. Every request
 * goes to the API gateway — this app never addresses an individual service
 * directly, the same rule the backend services follow with each other.
 */
const DEFAULT_API_BASE_URL = 'http://localhost:8080'

export function apiBaseUrl(): string {
  return import.meta.env.VITE_API_BASE_URL ?? DEFAULT_API_BASE_URL
}

/** Thrown for any non-2xx response. Carries the HTTP status and, where the
 * backend returned one, the human-readable "error" field from its
 * {status, error, timestamp} error body (see each service's
 * GlobalExceptionHandler) rather than a generic "request failed" message. */
export class ApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
}

interface ErrorBody {
  status?: number
  error?: string
  timestamp?: string
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await fetch(`${apiBaseUrl()}${path}`, {
    method: options.method ?? 'GET',
    headers: { 'Content-Type': 'application/json' },
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  })

  const contentType = response.headers.get('content-type') ?? ''
  const payload: unknown = contentType.includes('application/json') ? await response.json() : null

  if (!response.ok) {
    const errorBody = payload as ErrorBody | null
    const message = errorBody?.error ?? `Request failed with status ${response.status}`
    throw new ApiError(message, response.status)
  }

  return payload as T
}
