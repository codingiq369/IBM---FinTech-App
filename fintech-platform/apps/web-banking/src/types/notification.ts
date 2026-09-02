/** Mirrors notification-orchestrator's NotificationResponse. Not scoped to
 * a customer or account — notification-orchestrator only knows "an event
 * arrived", not who it's for, so this is the platform's activity feed as a
 * whole rather than a personal inbox. See docs/domains/notifications.md. */
export interface NotificationRecord {
  id: string
  eventType: string
  referenceId: string
  summary: string
  receivedAt: string
}
