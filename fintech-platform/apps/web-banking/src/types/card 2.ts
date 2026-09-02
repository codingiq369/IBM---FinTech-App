export type CardStatus = 'ISSUED' | 'ACTIVE' | 'BLOCKED' | 'CLOSED'
export type CardType = 'DEBIT' | 'CREDIT'

/** Mirrors card-management-service's CardResponse. */
export interface Card {
  id: string
  accountId: string
  customerId: string
  cardNumberMasked: string
  cardholderName: string
  cardType: CardType
  expiryMonth: number
  expiryYear: number
  status: CardStatus
  dailyPurchaseLimit: number
  createdAt: string
  activatedAt: string | null
  blockedAt: string | null
}

export type CardAuthorizationStatus = 'APPROVED' | 'DECLINED'

/** Mirrors card-authorization-service's CardAuthorizationResponse. */
export interface CardAuthorization {
  id: string
  cardId: string
  accountId: string
  merchantName: string
  amount: number
  currency: string
  status: CardAuthorizationStatus
  journalEntryReference: string | null
  declineReason: string | null
  createdAt: string
}
