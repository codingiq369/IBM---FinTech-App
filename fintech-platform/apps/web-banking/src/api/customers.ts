import { apiRequest } from './httpClient'
import type { Customer } from '../types'

export interface OnboardCustomerInput {
  fullName: string
  email: string
  dateOfBirth: string
}

export function onboardCustomer(input: OnboardCustomerInput): Promise<Customer> {
  return apiRequest<Customer>('/api/customers', { method: 'POST', body: input })
}

export function getCustomer(id: string): Promise<Customer> {
  return apiRequest<Customer>(`/api/customers/${id}`)
}
