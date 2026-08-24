import { apiRequest } from './httpClient'
import type { Transfer } from '../types'

export interface InitiateTransferInput {
  sourceAccountId: string
  destinationAccountId: string
  amount: number
  description?: string
}

export function initiateTransfer(input: InitiateTransferInput): Promise<Transfer> {
  return apiRequest<Transfer>('/api/transfers', { method: 'POST', body: input })
}

export function getTransfersForAccount(accountId: string): Promise<Transfer[]> {
  return apiRequest<Transfer[]>(`/api/transfers?accountId=${accountId}`)
}
