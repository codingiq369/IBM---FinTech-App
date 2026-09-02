import { useCallback, useEffect, useState } from 'react'
import { getRecentNotifications } from '../../api/notifications'
import { ApiError } from '../../api/httpClient'
import { useToast } from '../../stores/ToastContext'
import type { NotificationRecord } from '../../types'
import { formatDateTime, shortId } from '../../utils/format'

const POLL_INTERVAL_MS = 5000

const EVENT_TYPE_LABELS: Record<string, string> = {
  TransferCompleted: 'Transfer completed',
  CardAuthorizationApproved: 'Card purchase approved',
}

/**
 * A live-ish view of notification-orchestrator's audit trail: every
 * TransferCompleted and CardAuthorizationApproved event it has consumed
 * from Kafka's transaction-events topic (see ADR-0003). Polls on a plain
 * interval rather than pushing updates over a websocket — matching the
 * "smallest possible taste of event-driven architecture" scope of this
 * sprint, the same way the backend consumer itself is a log-and-persist
 * step, not a full notification-delivery system yet.
 *
 * Unlike every other page in this app, this feed is not scoped to
 * `currentCustomer` — notification-orchestrator has no notion of which
 * customer an event belongs to (see docs/domains/notifications.md), so
 * this shows platform-wide activity, not "your" activity. That's called
 * out in the empty state below rather than left implicit.
 */
export function NotificationsPage() {
  const { showToast } = useToast()
  const [notifications, setNotifications] = useState<NotificationRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [lastError, setLastError] = useState<string | null>(null)

  const refresh = useCallback(async () => {
    try {
      setNotifications(await getRecentNotifications())
      setLastError(null)
    } catch (error) {
      // Polling failures shouldn't spam a toast every 5 seconds — the
      // banner below is enough, and refresh() will clear it the moment
      // the gateway/notification-orchestrator is reachable again.
      setLastError(error instanceof ApiError ? error.message : 'Could not reach notification-orchestrator.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void refresh()
    const interval = setInterval(() => void refresh(), POLL_INTERVAL_MS)
    return () => clearInterval(interval)
  }, [refresh])

  async function handleManualRefresh() {
    setLoading(true)
    await refresh()
    if (!lastError) showToast('Refreshed.', 'success')
  }

  return (
    <section className="card">
      <h2>Activity</h2>
      <p className="hint">
        Every TransferCompleted and CardAuthorizationApproved event notification-orchestrator has consumed off
        Kafka's <code>transaction-events</code> topic, newest first — platform-wide, not filtered to the current
        customer, since that's all the event tells this service. Refreshes automatically every 5 seconds.
      </p>

      {lastError && <p className="gateway-status gateway-status--error">{lastError}</p>}

      <div className="row">
        <button type="button" onClick={() => void handleManualRefresh()} disabled={loading}>
          {loading ? 'Refreshing…' : 'Refresh now'}
        </button>
      </div>

      {loading && notifications.length === 0 ? (
        <p className="hint">Loading…</p>
      ) : notifications.length === 0 ? (
        <p className="hint">
          No activity recorded yet. Complete a transfer on the <strong>Transfer money</strong> page or approve a
          card purchase on the <strong>Cards</strong> page, then check back here.
        </p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>When</th>
              <th>Event</th>
              <th>Reference</th>
              <th>Summary</th>
            </tr>
          </thead>
          <tbody>
            {notifications.map((notification) => (
              <tr key={notification.id}>
                <td>{formatDateTime(notification.receivedAt)}</td>
                <td>
                  <span className="status-pill status-pill--neutral">
                    {EVENT_TYPE_LABELS[notification.eventType] ?? notification.eventType}
                  </span>
                </td>
                <td>{shortId(notification.referenceId)}</td>
                <td>{notification.summary}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
