import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'

export type ToastKind = 'info' | 'success' | 'error'

interface ToastState {
  id: number
  message: string
  kind: ToastKind
}

interface ToastContextValue {
  toast: ToastState | null
  showToast: (message: string, kind?: ToastKind) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)
const AUTO_DISMISS_MS = 5000

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toast, setToast] = useState<ToastState | null>(null)

  const showToast = useCallback((message: string, kind: ToastKind = 'info') => {
    const id = Date.now()
    setToast({ id, message, kind })
    setTimeout(() => {
      // Only clear it if a newer toast hasn't already replaced it.
      setToast((current) => (current?.id === id ? null : current))
    }, AUTO_DISMISS_MS)
  }, [])

  return <ToastContext.Provider value={{ toast, showToast }}>{children}</ToastContext.Provider>
}

export function useToast(): ToastContextValue {
  const context = useContext(ToastContext)
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider')
  }
  return context
}
