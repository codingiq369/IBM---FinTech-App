import { useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { getTransfersForAccount } from '../../api/transfers'
import { ApiError } from '../../api/httpClient'
import { StatusPill } from '../../components/StatusPill'
import { useDirectory } from '../../stores/DirectoryContext'
import { useToast } from '../../stores/ToastContext'
import type { Transfer } from '../../types'
import { formatDateTime, formatMoney } from '../../utils/format'

export function TransactionsPage() {
  const { accounts } = useDirectory()
  const { showToast } = useToast()
  const [searchParams, setSearchParams] = useSearchParams()

  const accountId = searchParams.get('accountId') ?? ''
  const [transfers, setTransfers] = useState<Transfer[]>([])
  const [loading, setLoading] = useState(false)

  const accountLabel = useCallback(
    (id: string) => accounts.find((a) => a.id === id)?.accountNumber ?? `${id.slice(0, 8)}…`,
    [accounts],
  )

  useEffect(() => {
    if (!accountId) {
      setTransfers([])
      return
    }
    setLoading(true)
    getTransfersForAccount(accountId)
      .then(setTransfers)
      .catch((error: unknown) => {
        showToast(error instanceof ApiError ? error.message : 'Could not load transfer history.', 'error')
      })
      .finally(() => setLoading(false))
  }, [accountId, showToast])

  return (
    <section className="card">
      <h2>Transaction history</h2>

      <label>
        Account
        <select value={accountId} onChange={(e) => setSearchParams(e.target.value ? { accountId: e.target.value } : {})}>
          <option value="">Select an account</option>
          {accounts.map((account) => (
            <option key={account.id} value={account.id}>
              {account.accountNumber} — {account.customerName}
            </option>
          ))}
        </select>
      </label>

      {!accountId ? (
        <p className="hint">Pick an account above to see its transfer history.</p>
      ) : loading ? (
        <p className="hint">Loading…</p>
      ) : transfers.length === 0 ? (
        <p className="hint">No transfers yet for this account.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>When</th>
              <th>From → To</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Detail</th>
            </tr>
          </thead>
          <tbody>
            {transfers.map((transfer) => (
              <tr key={transfer.id}>
                <td>{formatDateTime(transfer.createdAt)}</td>
                <td>
                  {accountLabel(transfer.sourceAccountId)} → {accountLabel(transfer.destinationAccountId)}
                </td>
                <td>{formatMoney(transfer.amount, transfer.currency)}</td>
                <td>
                  <StatusPill status={transfer.status} />
                </td>
                <td>{transfer.failureReason ?? (transfer.journalEntryReference ? 'Posted to ledger' : '')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
