import { Route, Routes } from 'react-router-dom'
import { AppLayout } from '../layouts/AppLayout'
import { AccountsPage } from '../pages/accounts/AccountsPage'
import { CardsPage } from '../pages/cards/CardsPage'
import { DashboardPage } from '../pages/dashboard/DashboardPage'
import { NotificationsPage } from '../pages/notifications/NotificationsPage'
import { PaymentsPage } from '../pages/payments/PaymentsPage'
import { ProfilePage } from '../pages/profile/ProfilePage'
import { TransactionsPage } from '../pages/transactions/TransactionsPage'

/**
 * Only the pages this platform's two vertical slices (plus the
 * event-driven notifications sprint built on top of them) actually
 * implement get a route. The scaffold's other page folders (loans,
 * investments, profile's siblings like security/support/statements) stay
 * as empty placeholders — see docs/architecture/vertical-slice.md for
 * what's next. `notifications` has no matching placeholder folder in the
 * original scaffold's page list — it's a net-new page, not a filled-in
 * one, mirroring notification-orchestrator being a net-new service on the
 * backend (see ADR-0003).
 */
export function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<DashboardPage />} />
        <Route path="profile" element={<ProfilePage />} />
        <Route path="accounts" element={<AccountsPage />} />
        <Route path="payments" element={<PaymentsPage />} />
        <Route path="transactions" element={<TransactionsPage />} />
        <Route path="cards" element={<CardsPage />} />
        <Route path="notifications" element={<NotificationsPage />} />
      </Route>
    </Routes>
  )
}
