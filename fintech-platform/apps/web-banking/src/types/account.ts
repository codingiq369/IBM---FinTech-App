export type AccountType = 'CHECKING' | 'SAVINGS'
export type AccountStatus = 'ACTIVE' | 'CLOSED'

/** Mirrors account-service's AccountResponse. */
export interface Account {
  id: string
  customerId: string
  accountNumber: string
  accountType: AccountType
  currency: string
  status: AccountStatus
  createdAt: string
}

/** Mirrors account-service's AccountBalanceResponse (fetched from ledger-service). */
export interface AccountBalance {
  accountId: string
  accountNumber: string
  balance: number
  currency: string
}

/** UI-only convenience shape: an account plus the balance we fetched for it,
 * so components don't need to juggle two separate loading states per row. */
export interface AccountWithBalance extends Account {
  balance: number | null
}
