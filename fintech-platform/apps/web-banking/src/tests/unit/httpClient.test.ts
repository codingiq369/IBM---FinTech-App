import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError, apiRequest } from '../../api/httpClient'

function jsonResponse(body: unknown, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'content-type': 'application/json' },
  })
}

describe('apiRequest', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('returns the parsed JSON body on a 2xx response', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ id: '123' }, 201)))

    const result = await apiRequest<{ id: string }>('/api/customers', { method: 'POST', body: { fullName: 'Ada' } })

    expect(result).toEqual({ id: '123' })
  })

  it('throws an ApiError carrying the backend’s "error" message on a non-2xx response', async () => {
    // Matches the {status, error, timestamp} shape every service's
    // GlobalExceptionHandler returns.
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(jsonResponse({ status: 422, error: 'Customer is not approved', timestamp: '2026-01-01T00:00:00Z' }, 422)),
    )

    await expect(apiRequest('/api/accounts', { method: 'POST', body: {} })).rejects.toMatchObject(
      new ApiError('Customer is not approved', 422),
    )
  })

  it('falls back to a generic message when the error response has no JSON body', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(null, { status: 503 })))

    await expect(apiRequest('/api/accounts')).rejects.toMatchObject({ status: 503, message: 'Request failed with status 503' })
  })
})
