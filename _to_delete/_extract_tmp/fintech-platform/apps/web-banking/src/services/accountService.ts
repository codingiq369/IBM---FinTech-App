import { getAccountBalance, getAccountsForCustomer } from '../api/accounts'
import type { AccountWithBalance } from '../types'

/**
 * account-service only tells you which accounts exist; the balance for each
 * one is a separate call to ledger-service (proxied through account-service).
 * This function is the one place that stitches "list of accounts" and
 * "balance per account" together, so page components don't each have to
 * re-implement the same fan-out-and-merge logic.
 */
export async function fetchAccountsWithBalances(customerId: string): Promise<AccountWithBalance[]> {
  const accounts = await getAccountsForCustomer(customerId)

  return Promise.all(
    accounts.map(async (account) => {
      try {
        const { balance } = await getAccountBalance(account.id)
        return { ...account, balance }
      } catch {
        // If the balance lookup fails (e.g. ledger-service is briefly
        // unreachable), still show the account — just without a balance —
        // rather than losing the whole list over one bad fetch.
        return { ...account, balance: null }
      }
    }),
  )
}
