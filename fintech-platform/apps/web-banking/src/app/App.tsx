import { BrowserRouter } from 'react-router-dom'
import { AppRoutes } from '../routes/AppRoutes'
import { DirectoryProvider } from '../stores/DirectoryContext'
import { ToastProvider } from '../stores/ToastContext'

export function App() {
  return (
    <DirectoryProvider>
      <ToastProvider>
        <BrowserRouter>
          <AppRoutes />
        </BrowserRouter>
      </ToastProvider>
    </DirectoryProvider>
  )
}
