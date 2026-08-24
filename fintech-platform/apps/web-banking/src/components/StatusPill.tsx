const POSITIVE = new Set(['ACTIVE', 'APPROVED', 'COMPLETED', 'UP'])
const NEGATIVE = new Set(['CLOSED', 'REJECTED', 'FAILED', 'DOWN'])

/** Colors any of our backend status strings consistently, without needing
 * to know every possible value up front — anything not explicitly positive
 * or negative (like PENDING) falls back to a neutral style. */
export function StatusPill({ status }: { status: string }) {
  const tone = POSITIVE.has(status) ? 'positive' : NEGATIVE.has(status) ? 'negative' : 'neutral'
  return <span className={`status-pill status-pill--${tone}`}>{status}</span>
}
