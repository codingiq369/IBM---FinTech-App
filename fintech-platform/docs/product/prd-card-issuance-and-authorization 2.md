# Product Requirements Document — Card Issuance & Authorization

**Version:** 1.0 · **Date:** 2026-09-02 · **Author:** Platform Engineering · **Status:** Approved

Filled from `templates/prd-template.md`. Traces to the FRD at
[`frd-card-issuance-and-authorization.md`](frd-card-issuance-and-authorization.md)
and to [ADR-0010](../architecture/architecture-decisions/ADR-0010-card-network-clearing-account.md).

## 1. Problem Statement

The platform's first vertical slice (`docs/architecture/vertical-slice.md`)
proves a customer can be onboarded, open an account, and move money between
accounts, all backed by a real double-entry ledger. It proves the pattern
works for one product. It does not yet prove the pattern *extends* — that a
second team, building a second product on the same platform, can add a new
domain without inventing a new architecture. Cards is the natural second
product to build: every retail banking platform needs one, and "can a
customer spend the money in their account" is a materially different
problem from "can a customer move money between two of their own
accounts" — it introduces a real-time authorization decision, a spending
limit, and a settlement counterparty that don't exist in the transfer flow.

## 2. Goals

- Prove the platform's service-per-domain, ledger-as-source-of-truth
  pattern extends cleanly to a second product built by (hypothetically) a
  different team, reusing accounts-service and ledger-service exactly as
  they stand today — no changes to either.
- Give a customer who already has an account a working debit card they can
  use to make a purchase, end to end, in the demo UI.
- Demonstrate a real-time authorization decision (not just a ledger
  posting): a card can be declined for reasons that have nothing to do
  with the ledger (inactive card, limit exceeded) as well as reasons that
  do (insufficient funds).

## 3. Non-Goals

- Credit cards, credit limits, or any repayment relationship (belongs to
  services/lending; see Out of Scope).
- Real card-network interchange, merchant acquiring, or interchange fees.
- Fraud scoring, velocity checks beyond the daily limit, or 3-D Secure.
- A physical card fulfillment/shipping workflow.
- PIN, CVV, or any PCI-DSS-scoped cardholder data storage.

## 4. Target Users / Personas

| Persona | Description | Primary need |
| --- | --- | --- |
| Retail banking customer | Already has an approved KYC status and at least one open account | Get a debit card linked to their account and use it to pay for something |
| Platform engineer (next team) | Building the *next* vertical slice (lending, trading, ...) | A second worked example of "how do I add a domain to this platform" beyond the original transfer flow |

## 5. User Stories

- As a customer with an active account, I want to request a debit card, so
  that I can spend my account balance without initiating a transfer first.
- As a customer, I want a newly issued card to require activation, so that
  a card can't be used for a purchase before I've confirmed I received it.
- As a customer, I want to be able to block my card, so that I can stop a
  lost or stolen card from being used.
- As a customer, I want my card purchases to be declined once I've spent
  my daily limit, so that a single compromised card can't drain my account
  in one sitting.
- As a customer, I want a purchase that would overdraw my account to be
  declined the same way a real bank would decline it, so that the ledger's
  no-negative-balance guarantee holds for card spending too, not just
  transfers.

## 6. Features & Requirements

| ID | Feature | Description | Priority | Traces to (FRD) |
| --- | --- | --- | --- | --- |
| PR-1 | Card issuance | Issue a DEBIT card against an ACTIVE account | Must | FR-1 |
| PR-2 | Card activation | Move a card from ISSUED to ACTIVE before it can authorize a purchase | Must | FR-2 |
| PR-3 | Card blocking | Move a card to BLOCKED, permanently disabling authorizations | Must | FR-3 |
| PR-4 | Real-time purchase authorization | Approve or decline a purchase synchronously, with a reason | Must | FR-4 |
| PR-5 | Daily purchase limit | Decline a purchase that would exceed the card's configured daily limit | Must | FR-5 |
| PR-6 | Ledger-backed settlement | Post an approved purchase as a balanced journal entry (debit cardholder, credit card network clearing account) | Must | FR-6 |
| PR-7 | Authorization history | List a card's past authorizations, approved and declined | Should | FR-7 |
| PR-8 | Demo UI | Issue, activate, block, and simulate a purchase from the web-banking app | Should | FR-8 |
| PR-9 | Card unblocking | Reverse a BLOCKED card back to ACTIVE | Won't (this release) | — |
| PR-10 | Credit cards | Issue a CREDIT-type card against a credit line | Won't (this release) | — |

## 7. Success Metrics

- A customer can go from "has an account" to "made an approved card
  purchase" in the demo UI without leaving the browser.
- `mvn test` is green for both new services, with the same category of
  proof the existing slice has for the ledger (`CardAuthorizationExecutionServiceTest`
  proves a card purchase can never be approved without a successful,
  balanced ledger posting, the same way `JournalEntryTest` proves a journal
  entry can never be unbalanced).
- Zero changes required to `accounts-service` or `ledger-service` to ship
  this feature — the strongest evidence the service boundaries from
  ADR-0001 hold up under a second real product.

## 8. Milestones / Timeline

| Milestone | Target date | Owner |
| --- | --- | --- |
| PRD/FRD/ADR approved | 2026-09-02 | Platform Engineering |
| card-management-service (issuance, activation, blocking) | 2026-09-02 | Platform Engineering |
| card-authorization-service (limit check + ledger posting) | 2026-09-02 | Platform Engineering |
| Gateway routing, docker-compose, environments wired | 2026-09-02 | Platform Engineering |
| web-banking Cards page | 2026-09-02 | Platform Engineering |
| Backlog items beyond this release (see `card-issuance-backlog.md`) | Unscheduled | Unassigned |

## 9. Dependencies

- `accounts-service` — the source of truth for whether an account exists
  and is active, and for the account's `ledgerAccountId` and currency.
- `ledger-service` — posts and enforces the balanced journal entry for
  every approved purchase; its no-negative-balance invariant
  (`JournalEntryTest`, `LedgerServiceTest`) is what makes a "decline on
  insufficient funds" possible without this feature reimplementing that
  check itself.
- `docs/architecture/vertical-slice.md` — the pattern (service boundaries,
  environment profiles, docker-compose wiring) this feature follows rather
  than reinvents.

## 10. Out of Scope

- Everything listed under Non-Goals above.
- `services/cards/card-dispute-service`, `card-rewards-service`,
  `card-tokenization-service`, `card-limits-service` remain empty
  scaffolding — this release only builds `card-management-service` and
  `card-authorization-service`, consolidating `card-issuance-service` and
  `card-activation-service` into the former and `card-authorization-service`
  and `card-transaction-service` into the latter, the same consolidation
  approach the original slice took for accounts and transfers.

## 11. Open Questions

- None outstanding at time of writing. (Card unblocking and credit cards
  are tracked as backlog items, not open questions — see
  `card-issuance-backlog.md`.)
