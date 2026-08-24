export type KycStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

/** Mirrors customer-service's CustomerResponse. */
export interface Customer {
  id: string
  fullName: string
  email: string
  dateOfBirth: string
  kycStatus: KycStatus
  createdAt: string
}
