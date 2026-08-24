import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchAccountsWithBalances } from '../../services/accountService'
import { useDirectory } from '../../stores/DirectoryContext'
import type { AccountWithBalance } from '../../types'
import { formatMoney } from '../../utils/format'

export function DashboardPage() {
  const { currentCustomer, accounts } = useDirectory()
  const [ownAccounts, setOwnAccounts] = useState<AccountWithBalance[]>([])

  useEffect(() => {
    if (!currentCustomer) {
      setOwnAccounts([])
      return
    }
    fetchAccountsWithBalances(currentCustomer.id)
      .then(setOwnAccounts)
      .catch(() => setOwnAccounts([]))
  }, [currentCustomer])

  if (!currentCustomer) {
    return (
      <section className="card">
        <h2>Welcome</h2>
        <p>
          This is a demo of one working path through the fintech platform scaffold: onboarding a customer,
          opening an account, and transferring money — all backed by a real double-entry ledger service.
        </p>
        <p>
          Start on the <Link to="/profile">Profile</Link> page to onboard your first customer.
        </p>
      </section>
    )
  }

  const total = ownAccounts.reduce((sum, account) => sum + (account.balance ?? 0), 0)
  const currency = ownAccounts[0]?.currency ?? 'USD'

  return (
    <>
      <section className="card">
        <h2>Welcome back, {currentCustomer.fullName}</h2>
        <p className="hint">
          {ownAccounts.length} account{ownAccounts.length === 1 ? '' : 's'} · known accounts across all
          customers in this demo: {accounts.length}
        </p>
        {ownAccounts.length > 0 && <p className="total-balance">{formatMoney(total, currency)}</p>}
      </section>

      <section className="card">
        <h2>Quick actions</h2>
        <div className="row">
          <Link className="button-link" to="/accounts">
            Open an account
          </Link>
          <Link className="button-link" to="/payments">
            Transfer money
          </Link>
          <Link className="button-link" to="/transactions">
            View transaction history
          </Link>
        </div>
      </section>
    </>
  )
}
