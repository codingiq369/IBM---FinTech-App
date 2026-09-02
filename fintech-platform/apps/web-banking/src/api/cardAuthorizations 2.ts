import { apiRequest } from './httpClient'
import type { CardAuthorization } from '../types'

export interface AuthorizePurchaseInput {
  cardId: string
  merchantName: string
  amount: number
  currency: string
}

/** Always resolves — a decline is a normal 201 response with
 * status: 'DECLINED', not a thrown ApiError. Only a malformed request
 * (e.g. an unknown cardId) throws. */
export function authorizePurchase(input: AuthorizePurchaseInput): Promise<CardAuthorization> {
  return apiRequest<CardAuthorization>('/api/card-authorizations', { method: 'POST', body: input })
}

export function getAuthorizationsForCard(cardId: string): Promise<CardAuthorization[]> {
  return apiRequest<CardAuthorization[]>(`/api/card-authorizations?cardId=${cardId}`)
}
