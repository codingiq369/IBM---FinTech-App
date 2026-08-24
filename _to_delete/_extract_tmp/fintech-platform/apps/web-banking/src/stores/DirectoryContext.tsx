import { createContext, useContext, useEffect, useMemo, useReducer, type ReactNode } from 'react'

/**
 * This "directory" is a UI-only convenience — it remembers, in this browser
 * only, which customers and accounts you've created during the demo so the
 * transfer form has something to pick from. It is NOT a cache of server
 * state: balances, statuses, and transfer history are always re-fetched
 * from the API. Losing this (via "Reset demo data" or clearing the browser)
 * never loses anything the backend knows about.
 */

export interface DirectoryCustomer {
  id: string
  fullName: string
  kycStatus: string
}

export interface DirectoryAccount {
  id: string
  customerId: string
  customerName: string
  accountNumber: string
  accountType: string
  currency: string
}

interface DirectoryState {
  customers: DirectoryCustomer[]
  accounts: DirectoryAccount[]
  currentCustomerId: string | null
}

const EMPTY_STATE: DirectoryState = { customers: [], accounts: [], currentCustomerId: null }
const STORAGE_KEY = 'fintechDemoDirectory'

function loadInitialState(): DirectoryState {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) return EMPTY_STATE
    const parsed: unknown = JSON.parse(raw)
    return isDirectoryState(parsed) ? parsed : EMPTY_STATE
  } catch {
    return EMPTY_STATE
  }
}

function isDirectoryState(value: unknown): value is DirectoryState {
  return (
    typeof value === 'object' &&
    value !== null &&
    Array.isArray((value as DirectoryState).customers) &&
    Array.isArray((value as DirectoryState).accounts)
  )
}

type DirectoryAction =
  | { type: 'ADD_CUSTOMER'; customer: DirectoryCustomer }
  | { type: 'SET_CURRENT_CUSTOMER'; customerId: string }
  | { type: 'ADD_ACCOUNT'; account: DirectoryAccount }
  | { type: 'RESET' }

function reducer(state: DirectoryState, action: DirectoryAction): DirectoryState {
  switch (action.type) {
    case 'ADD_CUSTOMER':
      return { ...state, customers: [...state.customers, action.customer], currentCustomerId: action.customer.id }
    case 'SET_CURRENT_CUSTOMER':
      return { ...state, currentCustomerId: action.customerId }
    case 'ADD_ACCOUNT':
      return { ...state, accounts: [...state.accounts, action.account] }
    case 'RESET':
      return EMPTY_STATE
  }
}

interface DirectoryContextValue {
  customers: DirectoryCustomer[]
  accounts: DirectoryAccount[]
  currentCustomerId: string | null
  currentCustomer: DirectoryCustomer | null
  addCustomer: (customer: DirectoryCustomer) => void
  setCurrentCustomer: (customerId: string) => void
  addAccount: (account: DirectoryAccount) => void
  reset: () => void
}

const DirectoryContext = createContext<DirectoryContextValue | null>(null)

export function DirectoryProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(reducer, undefined, loadInitialState)

  useEffect(() => {
    try {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
    } catch {
      // Storage can fail (private browsing, quota) — the app still works,
      // it just won't remember the directory across a reload.
    }
  }, [state])

  const value = useMemo<DirectoryContextValue>(
    () => ({
      customers: state.customers,
      accounts: state.accounts,
      currentCustomerId: state.currentCustomerId,
      currentCustomer: state.customers.find((c) => c.id === state.currentCustomerId) ?? null,
      addCustomer: (customer) => dispatch({ type: 'ADD_CUSTOMER', customer }),
      setCurrentCustomer: (customerId) => dispatch({ type: 'SET_CURRENT_CUSTOMER', customerId }),
      addAccount: (account) => dispatch({ type: 'ADD_ACCOUNT', account }),
      reset: () => dispatch({ type: 'RESET' }),
    }),
    [state],
  )

  return <DirectoryContext.Provider value={value}>{children}</DirectoryContext.Provider>
}

export function useDirectory(): DirectoryContextValue {
  const context = useContext(DirectoryContext)
  if (!context) {
    throw new Error('useDirectory must be used within a DirectoryProvider')
  }
  return context
}
