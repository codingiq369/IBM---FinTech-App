import { useEffect, useState } from 'react'
import { apiBaseUrl, checkGatewayHealth } from '../api'

type Status = 'checking' | 'up' | 'down'

/** Just enough of a health check to tell the user "start docker compose"
 * instead of leaving them staring at silent fetch failures on every page. */
export function GatewayStatusBanner() {
  const [status, setStatus] = useState<Status>('checking')

  useEffect(() => {
    let cancelled = false
    checkGatewayHealth()
      .then((health) => {
        if (!cancelled) setStatus(health.status === 'UP' ? 'up' : 'down')
      })
      .catch(() => {
        if (!cancelled) setStatus('down')
      })
    return () => {
      cancelled = true
    }
  }, [])

  if (status === 'checking') {
    return <p className="gateway-status">Checking connection to the API gateway…</p>
  }
  if (status === 'up') {
    return <p className="gateway-status gateway-status--ok">Connected to the API gateway at {apiBaseUrl()}</p>
  }
  return (
    <p className="gateway-status gateway-status--error">
      Can't reach the API gateway at {apiBaseUrl()} — is docker compose up?
    </p>
  )
}
