import { Route, Routes } from 'react-router-dom'
import { AppLayout } from '../layouts/AppLayout'
import { AccountsPage } from '../pages/accounts/AccountsPage'
import { DashboardPage } from '../pages/dashboard/DashboardPage'
import { PaymentsPage } from '../pages/payments/PaymentsPage'
import { ProfilePage } from '../pages/profile/ProfilePage'
import { TransactionsPage } from '../pages/transactions/TransactionsPage'

/**
 * Only the pages this vertical slice actually implements get a route.
 * The scaffold's other page folders (cards, loans, investments, profile's
 * siblings like security/support/statements) stay as empty placeholders —
 * see docs/architecture/vertical-slice.md for what's next.
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
      </Route>
    </Routes>
  )
}
