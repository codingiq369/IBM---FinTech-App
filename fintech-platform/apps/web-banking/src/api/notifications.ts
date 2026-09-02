import { apiRequest } from './httpClient'
import type { NotificationRecord } from '../types'

/** The 50 most recent notifications recorded by notification-orchestrator,
 * newest first. There's no per-customer filter here — see the type's own
 * comment for why. */
export function getRecentNotifications(): Promise<NotificationRecord[]> {
  return apiRequest<NotificationRecord[]>('/api/notifications')
}
