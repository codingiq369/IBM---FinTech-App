import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { openAccount } from '../../api/accounts'
import { ApiError } from '../../api/httpClient'
import { fetchAccountsWithBalances } from '../../services/accountService'
import { StatusPill } from '../../components/StatusPill'
import { useDirectory } from '../../stores/DirectoryContext'
import { useToast } from '../../stores/ToastContext'
import type { AccountType, AccountWithBalance } from '../../types'
import { formatMoney } from '../../utils/format'

export function AccountsPage() {
  const { currentCustomer, addAccount } = useDirectory()
  const { showToast } = useToast()

  const [accounts, setAccounts] = useState<AccountWithBalance[]>([])
  const [loading, setLoading] = useState(false)
  const [accountType, setAccountType] = useState<AccountType>('CHECKING')
  const [currency, setCurrency] = useState('USD')
  const [opening, setOpening] = useState(false)

  const refresh = useCallback(async () => {
    if (!currentCustomer) {
      setAccounts([])
      return
    }
    setLoading(true)
    try {
      setAccounts(await fetchAccountsWithBalances(currentCustomer.id))
    } catch (error) {
      showToast(error instanceof ApiError ? error.message : 'Could not load accounts.', 'error')
    } finally {
      setLoading(false)
    }
  }, [currentCustomer, showToast])

  useEffect(() => {
    void refresh()
  }, [refresh])

  async function handleOpenAccount() {
    if (!currentCustomer) return
    setOpening(true)
    try {
      const account = await openAccount({ customerId: currentCustomer.id, accountType, currency })
      addAccount({
        id: account.id,
        customerId: currentCustomer.id,
        customerName: currentCustomer.fullName,
        accountNumber: account.accountNumber,
        accountType: account.accountType,
        currency: account.currency,
      })
      showToast(`Opened ${accountType.toLowerCase()} account ${account.accountNumber}.`, 'success')
      await refresh()
    } catch (error) {
      showToast(error instanceof ApiError ? error.message : 'Could not open the account.', 'error')
    } finally {
      setOpening(false)
    }
  }

  if (!currentCustomer) {
    return (
      <section className="card">
        <h2>Accounts</h2>
        <p className="hint">
          No active customer yet. Head to <Link to="/profile">Profile</Link> to onboard one first.
        </p>
      </section>
    )
  }

  return (
    <section className="card">
      <h2>Accounts for {currentCustomer.fullName}</h2>

      <div className="row">
        <label>
          Account type
          <select value={accountType} onChange={(e) => setAccountType(e.target.value as AccountType)}>
            <option value="CHECKING">Checking</option>
            <option value="SAVINGS">Savings</option>
          </select>
        </label>
        <label>
          Currency
          <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
            <option value="USD">USD</option>
            <option value="EUR">EUR</option>
          </select>
        </label>
        <button type="button" onClick={() => void handleOpenAccount()} disabled={opening}>
          {opening ? 'Opening…' : 'Open account'}
        </button>
      </div>

      {loading ? (
        <p className="hint">Loading accounts…</p>
      ) : accounts.length === 0 ? (
        <p className="hint">No accounts yet for this customer.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Account #</th>
              <th>Type</th>
              <th>Balance</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {accounts.map((account) => (
              <tr key={account.id}>
                <td>{account.accountNumber}</td>
                <td>{account.accountType}</td>
                <td>{account.balance !== null ? formatMoney(account.balance, account.currency) : '—'}</td>
                <td>
                  <StatusPill status={account.status} />
                </td>
                <td>
                  <Link className="link" to={`/transactions?accountId=${account.id}`}>
                    History
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
