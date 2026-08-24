import { describe, expect, it } from 'vitest'
import { formatMoney, shortId } from '../../utils/format'

describe('formatMoney', () => {
  it('formats a USD amount with the currency symbol and two decimals', () => {
    expect(formatMoney(25, 'USD')).toBe('$25.00')
  })

  it('formats a non-USD currency using its own symbol', () => {
    expect(formatMoney(10.5, 'EUR')).toBe('€10.50')
  })
})

describe('shortId', () => {
  it('returns just the first segment of a UUID', () => {
    expect(shortId('3f9a2c1e-1234-5678-9abc-def012345678')).toBe('3f9a2c1e')
  })
})
