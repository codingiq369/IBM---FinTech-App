import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { initiateTransfer } from '../../api/transfers'
import { ApiError } from '../../api/httpClient'
import { useDirectory } from '../../stores/DirectoryContext'
import { useToast } from '../../stores/ToastContext'
import { formatMoney } from '../../utils/format'

export function PaymentsPage() {
  const { accounts } = useDirectory()
  const { showToast } = useToast()
  const navigate = useNavigate()

  const [sourceAccountId, setSourceAccountId] = useState('')
  const [destinationAccountId, setDestinationAccountId] = useState('')
  const [amount, setAmount] = useState('')
  const [description, setDescription] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (accounts.length < 2) {
    return (
      <section className="card">
        <h2>Transfer money</h2>
        <p className="hint">
          You need at least two accounts to transfer between (they can belong to different customers). Open
          more on the <Link to="/accounts">Accounts</Link> page.
        </p>
      </section>
    )
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!sourceAccountId || !destinationAccountId) {
      showToast('Choose both a source and a destination account.', 'error')
      return
    }
    setSubmitting(true)
    try {
      const transfer = await initiateTransfer({
        sourceAccountId,
        destinationAccountId,
        amount: Number(amount),
        description: description || undefined,
      })
      if (transfer.status === 'COMPLETED') {
        showToast(`Transfer completed: ${formatMoney(transfer.amount, transfer.currency)}.`, 'success')
      } else {
        showToast(`Transfer failed: ${transfer.failureReason ?? 'unknown reason'}`, 'error')
      }
      setAmount('')
      setDescription('')
      navigate(`/transactions?accountId=${sourceAccountId}`)
    } catch (error) {
      // A thrown ApiError here means the *request* was invalid (bad
      // account, currency mismatch) — a FAILED transfer status above is a
      // different, successfully-recorded outcome, not an exception.
      showToast(error instanceof ApiError ? error.message : 'Could not submit the transfer.', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="card">
      <h2>Transfer money</h2>
      <form onSubmit={handleSubmit}>
        <label>
          From
          <select value={sourceAccountId} onChange={(e) => setSourceAccountId(e.target.value)} required>
            <option value="" disabled>
              Select an account
            </option>
            {accounts.map((account) => (
              <option key={account.id} value={account.id}>
                {account.accountNumber} — {account.customerName} ({account.currency})
              </option>
            ))}
          </select>
        </label>
        <label>
          To
          <select value={destinationAccountId} onChange={(e) => setDestinationAccountId(e.target.value)} required>
            <option value="" disabled>
              Select an account
            </option>
            {accounts.map((account) => (
              <option key={account.id} value={account.id}>
                {account.accountNumber} — {account.customerName} ({account.currency})
              </option>
            ))}
          </select>
        </label>
        <label>
          Amount
          <input type="number" min="0.01" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required placeholder="25.00" />
        </label>
        <label>
          Description
          <input value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Rent" />
        </label>
        <button type="submit" disabled={submitting}>
          {submitting ? 'Sending…' : 'Send transfer'}
        </button>
      </form>
    </section>
  )
}
