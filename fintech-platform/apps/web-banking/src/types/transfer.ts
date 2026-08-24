export type TransferStatus = 'PENDING' | 'COMPLETED' | 'FAILED'

/** Mirrors transfers-service's TransferResponse. */
export interface Transfer {
  id: string
  sourceAccountId: string
  destinationAccountId: string
  amount: number
  currency: string
  status: TransferStatus
  journalEntryReference: string | null
  failureReason: string | null
  createdAt: string
  updatedAt: string
}
