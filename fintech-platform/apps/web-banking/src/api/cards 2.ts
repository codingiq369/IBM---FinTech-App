import { apiRequest } from './httpClient'
import type { Card } from '../types'

export interface IssueCardInput {
  accountId: string
  cardholderName: string
  dailyPurchaseLimit?: number
}

export function issueCard(input: IssueCardInput): Promise<Card> {
  return apiRequest<Card>('/api/cards', { method: 'POST', body: input })
}

export function activateCard(cardId: string): Promise<Card> {
  return apiRequest<Card>(`/api/cards/${cardId}/activate`, { method: 'POST' })
}

export function blockCard(cardId: string): Promise<Card> {
  return apiRequest<Card>(`/api/cards/${cardId}/block`, { method: 'POST' })
}

export function getCardsForCustomer(customerId: string): Promise<Card[]> {
  return apiRequest<Card[]>(`/api/cards?customerId=${customerId}`)
}
