import { NavLink, Outlet } from 'react-router-dom'
import { GatewayStatusBanner } from '../components/GatewayStatusBanner'
import { Toast } from '../components/Toast'
import { ROUTES } from '../constants/routes'

const NAV_ITEMS = [
  { to: ROUTES.dashboard, label: 'Dashboard' },
  { to: ROUTES.profile, label: 'Profile' },
  { to: ROUTES.accounts, label: 'Accounts' },
  { to: ROUTES.payments, label: 'Transfer money' },
  { to: ROUTES.transactions, label: 'Transaction history' },
  { to: ROUTES.cards, label: 'Cards' },
]

export function AppLayout() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <h1>FinTech Platform</h1>
        <p className="subtitle">
          Web banking demo — walks both vertical slices: onboard a customer, open an account, transfer money,
          issue a debit card, and authorize a purchase, all backed by one honest ledger.
        </p>
        <GatewayStatusBanner />
        <nav className="app-nav">
          {NAV_ITEMS.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === ROUTES.dashboard}>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </header>

      <main className="app-main">
        <Outlet />
      </main>

      <Toast />
    </div>
  )
}
