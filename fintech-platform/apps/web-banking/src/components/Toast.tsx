import { useToast } from '../stores/ToastContext'

export function Toast() {
  const { toast } = useToast()
  if (!toast) return null

  return (
    <div className={`toast toast--${toast.kind}`} role="status">
      {toast.message}
    </div>
  )
}
