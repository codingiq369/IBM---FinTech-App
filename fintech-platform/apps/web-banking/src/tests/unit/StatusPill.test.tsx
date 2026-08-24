import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { StatusPill } from '../../components/StatusPill'

describe('StatusPill', () => {
  it('styles a known-good status as positive', () => {
    render(<StatusPill status="ACTIVE" />)
    expect(screen.getByText('ACTIVE')).toHaveClass('status-pill--positive')
  })

  it('styles a known-bad status as negative', () => {
    render(<StatusPill status="FAILED" />)
    expect(screen.getByText('FAILED')).toHaveClass('status-pill--negative')
  })

  it('falls back to neutral for anything else, like PENDING', () => {
    render(<StatusPill status="PENDING" />)
    expect(screen.getByText('PENDING')).toHaveClass('status-pill--neutral')
  })
})
