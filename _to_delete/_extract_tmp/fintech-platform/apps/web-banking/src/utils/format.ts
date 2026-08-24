export function formatMoney(amount: number, currency: string): string {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount)
}

export function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString()
}

/** Shortens a UUID for display, e.g. "3f9a2c1e-…" -> "3f9a2c1e". Used
 * wherever we need to show an id but the full UUID would just be noise. */
export function shortId(id: string): string {
  return id.split('-')[0]
}
