import { useState, type FormEvent } from 'react'
import { onboardCustomer } from '../../api/customers'
import { ApiError } from '../../api/httpClient'
import { StatusPill } from '../../components/StatusPill'
import { useDirectory } from '../../stores/DirectoryContext'
import { useToast } from '../../stores/ToastContext'

export function ProfilePage() {
  const { customers, currentCustomerId, currentCustomer, addCustomer, setCurrentCustomer, reset } = useDirectory()
  const { showToast } = useToast()

  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [dateOfBirth, setDateOfBirth] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setSubmitting(true)
    try {
      const customer = await onboardCustomer({ fullName, email, dateOfBirth })
      addCustomer({ id: customer.id, fullName: customer.fullName, kycStatus: customer.kycStatus })
      setFullName('')
      setEmail('')
      setDateOfBirth('')
      showToast(
        customer.kycStatus === 'APPROVED'
          ? `${customer.fullName} onboarded and KYC-approved.`
          : `${customer.fullName} onboarded but KYC ${customer.kycStatus.toLowerCase()} (must be 18+).`,
        customer.kycStatus === 'APPROVED' ? 'success' : 'error',
      )
    } catch (error) {
      showToast(error instanceof ApiError ? error.message : 'Something went wrong onboarding this customer.', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <section className="card">
        <h2>Onboard a customer</h2>
        <form onSubmit={handleSubmit}>
          <label>
            Full name
            <input value={fullName} onChange={(e) => setFullName(e.target.value)} required placeholder="Ada Lovelace" />
          </label>
          <label>
            Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required placeholder="ada@example.com" />
          </label>
          <label>
            Date of birth
            <input type="date" value={dateOfBirth} onChange={(e) => setDateOfBirth(e.target.value)} required />
          </label>
          <button type="submit" disabled={submitting}>
            {submitting ? 'Onboarding…' : 'Onboard customer'}
          </button>
        </form>
      </section>

      <section className="card">
        <h2>Active customer</h2>
        {customers.length === 0 ? (
          <p className="hint">No customers yet — onboard one above.</p>
        ) : (
          <div className="row">
            <label>
              Switch active customer
              <select value={currentCustomerId ?? ''} onChange={(e) => setCurrentCustomer(e.target.value)}>
                {customers.map((customer) => (
                  <option key={customer.id} value={customer.id}>
                    {customer.fullName} ({customer.kycStatus})
                  </option>
                ))}
              </select>
            </label>
            {currentCustomer && (
              <p>
                KYC status: <StatusPill status={currentCustomer.kycStatus} />
              </p>
            )}
          </div>
        )}
      </section>

      <section className="card">
        <h2>Demo data</h2>
        <p className="hint">
          This browser remembers which customers and accounts you've created so the other pages have
          something to pick from. It's not server data — resetting it doesn't touch the backend.
        </p>
        <button
          type="button"
          className="secondary"
          onClick={() => {
            reset()
            showToast("Cleared this browser's local demo directory. Server data is untouched.")
          }}
        >
          Reset demo data
        </button>
      </section>
    </>
  )
}
