import { act, renderHook } from '@testing-library/react'
import { beforeEach, describe, expect, it } from 'vitest'
import { DirectoryProvider, useDirectory } from '../../stores/DirectoryContext'
import type { ReactNode } from 'react'

function wrapper({ children }: { children: ReactNode }) {
  return <DirectoryProvider>{children}</DirectoryProvider>
}

describe('useDirectory', () => {
  beforeEach(() => {
    window.localStorage.clear()
  })

  it('adding a customer makes them the current customer', () => {
    const { result } = renderHook(() => useDirectory(), { wrapper })

    act(() => {
      result.current.addCustomer({ id: 'cust-1', fullName: 'Ada Lovelace', kycStatus: 'APPROVED' })
    })

    expect(result.current.currentCustomerId).toBe('cust-1')
    expect(result.current.currentCustomer?.fullName).toBe('Ada Lovelace')
  })

  it('adding an account appends it without disturbing existing accounts', () => {
    const { result } = renderHook(() => useDirectory(), { wrapper })

    act(() => {
      result.current.addAccount({
        id: 'acc-1',
        customerId: 'cust-1',
        customerName: 'Ada Lovelace',
        accountNumber: 'ACC-1',
        accountType: 'CHECKING',
        currency: 'USD',
      })
    })
    act(() => {
      result.current.addAccount({
        id: 'acc-2',
        customerId: 'cust-1',
        customerName: 'Ada Lovelace',
        accountNumber: 'ACC-2',
        accountType: 'SAVINGS',
        currency: 'USD',
      })
    })

    expect(result.current.accounts).toHaveLength(2)
    expect(result.current.accounts.map((a) => a.accountNumber)).toEqual(['ACC-1', 'ACC-2'])
  })

  it('reset clears everything back to empty', () => {
    const { result } = renderHook(() => useDirectory(), { wrapper })

    act(() => {
      result.current.addCustomer({ id: 'cust-1', fullName: 'Ada Lovelace', kycStatus: 'APPROVED' })
    })
    act(() => {
      result.current.reset()
    })

    expect(result.current.customers).toHaveLength(0)
    expect(result.current.currentCustomerId).toBeNull()
  })

  it('persists state to localStorage so a fresh provider picks it back up', () => {
    const { result, unmount } = renderHook(() => useDirectory(), { wrapper })

    act(() => {
      result.current.addCustomer({ id: 'cust-1', fullName: 'Ada Lovelace', kycStatus: 'APPROVED' })
    })
    unmount()

    const { result: secondResult } = renderHook(() => useDirectory(), { wrapper })
    expect(secondResult.current.customers).toHaveLength(1)
    expect(secondResult.current.customers[0].fullName).toBe('Ada Lovelace')
  })
})
