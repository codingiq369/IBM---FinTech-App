import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { activateCard, blockCard, getCardsForCustomer, issueCard } from '../../api/cards'
import { authorizePurchase, getAuthorizationsForCard } from '../../api/cardAuthorizations'
import { ApiError } from '../../api/httpClient'
import { StatusPill } from '../../components/StatusPill'
import { useDirectory } from '../../stores/DirectoryContext'
import { useToast } from '../../stores/ToastContext'
import type { Card, CardAuthorization } from '../../types'
import { formatDateTime, formatMoney } from '../../utils/format'

export function CardsPage() {
  const { currentCustomer, accounts } = useDirectory()
  const { showToast } = useToast()

  const [cards, setCards] = useState<Card[]>([])
  const [loading, setLoading] = useState(false)

  const [accountId, setAccountId] = useState('')
  const [dailyPurchaseLimit, setDailyPurchaseLimit] = useState('')
  const [issuing, setIssuing] = useState(false)
  const [busyCardId, setBusyCardId] = useState<string | null>(null)

  const [purchaseCardId, setPurchaseCardId] = useState('')
  const [merchantName, setMerchantName] = useState('')
  const [purchaseAmount, setPurchaseAmount] = useState('')
  const [authorizing, setAuthorizing] = useState(false)
  const [history, setHistory] = useState<CardAuthorization[]>([])

  const customerAccounts = accounts.filter((account) => account.customerId === currentCustomer?.id)
  const activeCards = cards.filter((card) => card.status === 'ACTIVE')

  const refreshCards = useCallback(async () => {
    if (!currentCustomer) {
      setCards([])
      return
    }
    setLoading(true)
    try {
      setCards(await getCardsForCustomer(currentCustomer.id))
    } catch (error) {
      showToast(error instanceof ApiError ? error.message : 'Could not load cards.', 'error')
    } finally {
      setLoading(false)
    }
  }, [currentCustomer, showToast])

  useEffect(() => {
    void refreshCards()
  }, [refreshCards])

  async function refreshHistory(cardId: string) {
    try {
      setHistory(await getAuthorizationsForCard(cardId))
    } catch {
      // Non-critical for the demo — the authorization result toast already
      // told the user what happened; a stale history list isn't worth a
      // second error toast.
    }
  }

  async function handleIssueCard(event: FormEvent) {
    event.preventDefault()
    if (!currentCustomer || !accountId) return
    setIssuing(true)
    try {
      const card = await issueCard({
        accountId,
        cardholderName: currentCustomer.fullName,
        dailyPurchaseLimit: dailyPurchaseLimit ? Number(dailyPurchaseLimit) : undefined,
      })
      showToast(`Issued card ${card.cardNumberMasked}. Activate it before using it.`, 'success')
      setDailyPurchaseLimit('')
      await refreshCards()
    } catch (error) {
      showToast(error instanceof ApiError ? error.message : 'Could not issue the card.', 'error')
    } finally {
      setIssuing(false)
    }
  }

  async function handleActivate(cardId: string) {
    setBusyCardId(cardId)
    try {
      await activateCard(cardId)
      showToast('Card activated.', 'success')
      await refreshCards()
    } catch (error) {
      showToast(error instanceof ApiError ? error.message : 'Could not activate the card.', 'error')
    } finally {
      setBusyCardId(null)
    }
  }

  async function handleBlock(cardId: string) {
    setBusyCardId(cardId)
    try {
      await blockCard(cardId)
      showToast('Card blocked.', 'success')
      await refreshCards()
    } catch (error) {
      showToast(error instanceof ApiError ? error.message : 'Could not block the card.', 'error')
    } finally {
      setBusyCardId(null)
    }
  }

  async function handleAuthorize(event: FormEvent) {
    event.preventDefault()
    if (!purchaseCardId) return
    const card = cards.find((c) => c.id === purchaseCardId)
    const account = customerAccounts.find((a) => a.id === card?.accountId)
    setAuthorizing(true)
    try {
      const authorization = await authorizePurchase({
        cardId: purchaseCardId,
        merchantName,
        amount: Number(purchaseAmount),
        currency: account?.currency ?? 'USD',
      })
      if (authorization.status === 'APPROVED') {
        showToast(`Approved: ${formatMoney(authorization.amount, authorization.currency)} at ${authorization.merchantName}.`, 'success')
      } else {
        showToast(`Declined: ${authorization.declineReason ?? 'unknown reason'}`, 'error')
      }
      setMerchantName('')
      setPurchaseAmount('')
      await refreshHistory(purchaseCardId)
    } catch (error) {
      // A thrown ApiError here means the *request* was invalid (no such
      // card) — a DECLINED status above is a different, successfully
      // recorded outcome, not an exception. Same distinction PaymentsPage
      // draws between a failed request and a FAILED transfer.
      showToast(error instanceof ApiError ? error.message : 'Could not submit the purchase.', 'error')
    } finally {
      setAuthorizing(false)
    }
  }

  if (!currentCustomer) {
    return (
      <section className="card">
        <h2>Cards</h2>
        <p className="hint">
          No active customer yet. Head to <Link to="/profile">Profile</Link> to onboard one first.
        </p>
      </section>
    )
  }

  return (
    <>
      <section className="card">
        <h2>Cards for {currentCustomer.fullName}</h2>

        {customerAccounts.length === 0 ? (
          <p className="hint">
            No accounts yet for this customer. Open one on the <Link to="/accounts">Accounts</Link> page before
            issuing a card.
          </p>
        ) : (
          <form onSubmit={handleIssueCard} className="row">
            <label>
              Account
              <select value={accountId} onChange={(e) => setAccountId(e.target.value)} required>
                <option value="" disabled>
                  Select an account
                </option>
                {customerAccounts.map((account) => (
                  <option key={account.id} value={account.id}>
                    {account.accountNumber} ({account.currency})
                  </option>
                ))}
              </select>
            </label>
            <label>
              Daily purchase limit
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={dailyPurchaseLimit}
                onChange={(e) => setDailyPurchaseLimit(e.target.value)}
                placeholder="2000.00"
              />
            </label>
            <button type="submit" disabled={issuing}>
              {issuing ? 'Issuing…' : 'Issue debit card'}
            </button>
          </form>
        )}

        {loading ? (
          <p className="hint">Loading cards…</p>
        ) : cards.length === 0 ? (
          <p className="hint">No cards yet for this customer.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Card</th>
                <th>Cardholder</th>
                <th>Expires</th>
                <th>Daily limit</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {cards.map((card) => (
                <tr key={card.id}>
                  <td>{card.cardNumberMasked}</td>
                  <td>{card.cardholderName}</td>
                  <td>
                    {String(card.expiryMonth).padStart(2, '0')}/{card.expiryYear}
                  </td>
                  <td>{formatMoney(card.dailyPurchaseLimit, customerAccounts.find((a) => a.id === card.accountId)?.currency ?? 'USD')}</td>
                  <td>
                    <StatusPill status={card.status} />
                  </td>
                  <td>
                    {card.status === 'ISSUED' && (
                      <button type="button" onClick={() => void handleActivate(card.id)} disabled={busyCardId === card.id}>
                        Activate
                      </button>
                    )}
                    {card.status === 'ACTIVE' && (
                      <button type="button" onClick={() => void handleBlock(card.id)} disabled={busyCardId === card.id}>
                        Block
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <section className="card">
        <h2>Simulate a purchase</h2>
        {activeCards.length === 0 ? (
          <p className="hint">Activate a card above to simulate a purchase against it.</p>
        ) : (
          <form onSubmit={handleAuthorize} className="row">
            <label>
              Card
              <select
                value={purchaseCardId}
                onChange={(e) => {
                  setPurchaseCardId(e.target.value)
                  void refreshHistory(e.target.value)
                }}
                required
              >
                <option value="" disabled>
                  Select a card
                </option>
                {activeCards.map((card) => (
                  <option key={card.id} value={card.id}>
                    {card.cardNumberMasked}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Merchant
              <input value={merchantName} onChange={(e) => setMerchantName(e.target.value)} required placeholder="Coffee Shop" />
            </label>
            <label>
              Amount
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={purchaseAmount}
                onChange={(e) => setPurchaseAmount(e.target.value)}
                required
                placeholder="4.50"
              />
            </label>
            <button type="submit" disabled={authorizing}>
              {authorizing ? 'Authorizing…' : 'Authorize purchase'}
            </button>
          </form>
        )}

        {history.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>When</th>
                <th>Merchant</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Reason</th>
              </tr>
            </thead>
            <tbody>
              {history.map((authorization) => (
                <tr key={authorization.id}>
                  <td>{formatDateTime(authorization.createdAt)}</td>
                  <td>{authorization.merchantName}</td>
                  <td>{formatMoney(authorization.amount, authorization.currency)}</td>
                  <td>
                    <StatusPill status={authorization.status} />
                  </td>
                  <td>{authorization.declineReason ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </>
  )
}
