import { apiRequest } from './httpClient'
import type { Account, AccountBalance, AccountType } from '../types'

export interface OpenAccountInput {
  customerId: string
  accountType: AccountType
  currency: string
}

export function openAccount(input: OpenAccountInput): Promise<Account> {
  return apiRequest<Account>('/api/accounts', { method: 'POST', body: input })
}

export function getAccountsForCustomer(customerId: string): Promise<Account[]> {
  return apiRequest<Account[]>(`/api/accounts?customerId=${customerId}`)
}

export function getAccountBalance(accountId: string): Promise<AccountBalance> {
  return apiRequest<AccountBalance>(`/api/accounts/${accountId}/balance`)
}
