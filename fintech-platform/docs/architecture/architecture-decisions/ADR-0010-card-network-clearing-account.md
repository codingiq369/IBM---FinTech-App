# ADR-0010: Card purchases settle against a card-network clearing ledger account

**Status:** Accepted
**Date:** 2026-09-02
**Deciders:** Platform Engineering

## Context

Card issuance & authorization (`docs/product/prd-card-issuance-and-authorization.md`)
needs to move money out of a cardholder's account the moment a purchase is
approved, using the same double-entry ledger that backs the existing
transfer flow. `ledger-service`'s core invariant — enforced by
`JournalEntry`'s constructor, not by caller discipline — is that every
journal entry balances: total debits equal total credits, across at least
two ledger accounts. `internal-transfer-service` satisfies this trivially,
because a transfer already has two real counterparties: the source
account and the destination account, both real bank accounts owned by
real customers.

A card purchase does not have that second real counterparty available in
this platform. In a real card network, the credit leg of a purchase
ultimately lands with the merchant's acquiring bank, net of interchange
and scheme fees, after settlement — a multi-party process involving
merchant-acquiring, interchange, and scheme settlement domains this
platform does not build (see the PRD's Non-Goals). Without *some* credit
leg, `card-authorization-service` cannot post a balanced entry at all,
which means it cannot reuse `ledger-service` unmodified — the one thing
this feature set out to prove was possible.

## Decision

Introduce one ledger account per currency, owned and lazily created by
`card-authorization-service`, that stands in for "the card network" as a
single settlement counterparty. An approved purchase posts a two-legged
journal entry: **debit** the cardholder's ledger account, **credit** this
clearing account, for the purchase amount. `ClearingAccountService` finds
or creates the account for a currency the first time it's needed
(`ClearingAccount(currency, ledgerAccountId)`, one row per currency in
`card-authorization-service`'s own database) and never asks
`ledger-service` to do anything it doesn't already do for any other
account.

Concretely, this means:

1. `ledger-service` is not modified in any way — no new concept, no new
   endpoint, no cards-specific logic. It sees `card-authorization-service`
   exactly the way it sees `internal-transfer-service`: a caller posting a
   balanced two-legged entry between two ledger accounts it already knows
   about.
2. The clearing account's `ownerReference` is a well-known synthetic value
   (`"CARD_NETWORK_CLEARING:" + currency`) — not tied to any real
   customer or account — so it's recognizable in the ledger's own data if
   ever inspected directly.
3. A race between two concurrent first-ever purchases in a brand-new
   currency is handled explicitly (a unique constraint on `currency` plus
   a re-read on conflict) rather than assumed away.

## Consequences

**What this buys:** `accounts-service` and `ledger-service` needed zero
changes to support a second product built on top of them — the strongest
available evidence that the service boundaries drawn in ADR-0001 and the
database-per-service isolation in ADR-0002 hold up under real extension,
not just under the one flow they were designed alongside. It also means
the "insufficient funds" decline path for a card purchase is the *exact
same* code path (`InsufficientFundsException` in `LedgerService`) as an
insufficient-funds transfer — one invariant, enforced in one place, for
both products.

**What this costs, on purpose:** the clearing account's balance is not
meaningful the way a real card network's settlement position is — it just
accumulates every approved purchase across every currency and customer,
forever, with no corresponding debit until a real
merchant-acquiring/interchange domain is built to actually pay merchants
out of it. That domain (net settlement, interchange fee splits, chargebacks
through `services/cards/card-dispute-service`) is explicitly out of scope
for this release (see the PRD) and is not something this ADR claims to
have solved — it names the simplification so the next team extending this
doesn't mistake the clearing account for a real settlement ledger.

## Alternatives considered

- **Give `ledger-service` a "system account" concept baked in.** Rejected:
  it would make `ledger-service` aware of a cards-specific concept,
  eroding the same boundary this decision is trying to prove holds. A
  clearing account is just an ordinary ledger account from
  `ledger-service`'s point of view — that's the point.
- **Single debit posting, no credit leg (skip balancing for cards).**
  Rejected outright: it would require weakening `JournalEntry`'s
  constructor invariant, the single most important guarantee in the
  repository (see `JournalEntryTest`) — not on the table for any feature.
- **Build a minimal merchant-acquiring domain now** (a real merchant
  ledger account per merchant). Rejected for this release as
  disproportionate scope for what the PRD is trying to prove; tracked as
  a backlog item (`docs/product/card-issuance-backlog.md`) for whoever
  builds `services/payments` or a dedicated merchant-settlement domain
  next.
