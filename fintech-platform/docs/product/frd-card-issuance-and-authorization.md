# Functional Requirements Document — Card Issuance & Authorization

**Version:** 1.0 · **Date:** 2026-09-02 · **Author:** Platform Engineering · **Status:** Approved

Filled from `templates/frd-template.md`. Traces to the PRD at
[`prd-card-issuance-and-authorization.md`](prd-card-issuance-and-authorization.md).

## 1. Introduction

This FRD turns the PRD's goals into the concrete, testable system behavior
implemented by `card-management-service` and `card-authorization-service`.
Each requirement below is implemented and covered by an automated test —
see the "Covered by" column and each service's README.

## 2. Scope

`services/cards/card-management-service` and
`services/cards/card-authorization-service`, their REST APIs, their
Postgres schemas, the two new routes on `gateways/api-gateway`, and the
Cards page in `apps/web-banking`. Does not cover any other folder under
`services/cards/`, which remains scaffolding.

## 3. Functional Requirements

### FR-1: Issue a card

- **Description:** `POST /api/cards` issues a new DEBIT card linked to an
  account.
- **Inputs:** `accountId`, `cardholderName`, optional `dailyPurchaseLimit`
  (defaults to 2000.00 in the account's currency).
- **Outputs:** `201 Created` with the new card, status `ISSUED`.
- **Business rules:** The account must exist and be `ACTIVE` (checked via
  `accounts-service`); otherwise `422` with no card created.
- **Acceptance criteria:**
  - Issuing against an `ACTIVE` account succeeds and returns `ISSUED`.
  - Issuing against a `CLOSED` account returns `422` and calls
    `accounts-service` but never persists a card.
  - Issuing against a nonexistent account returns `404`.
- **Covered by:** `CardServiceTest`.
- **Traces to:** PR-1.

### FR-2: Activate a card

- **Description:** `POST /api/cards/{id}/activate` moves a card from
  `ISSUED` to `ACTIVE`.
- **Inputs:** Card id.
- **Outputs:** `200 OK` with the updated card; `activatedAt` set.
- **Business rules:** Only a card in `ISSUED` status can be activated;
  any other starting status returns `422`.
- **Acceptance criteria:** Activating an `ISSUED` card succeeds; activating
  an already-`ACTIVE` card is rejected.
- **Covered by:** `CardServiceTest`.
- **Traces to:** PR-2.

### FR-3: Block a card

- **Description:** `POST /api/cards/{id}/block` moves a card to `BLOCKED`.
- **Inputs:** Card id.
- **Outputs:** `200 OK` with the updated card; `blockedAt` set.
- **Business rules:** Any status except `CLOSED` can be blocked. Blocking
  is terminal for authorization purposes in this release — there is no
  unblock endpoint (see PRD Out of Scope, PR-9).
- **Acceptance criteria:** A `BLOCKED` card can never subsequently
  authorize a purchase (see FR-4).
- **Covered by:** `CardServiceTest`.
- **Traces to:** PR-3.

### FR-4: Authorize a purchase

- **Description:** `POST /api/card-authorizations` decides, synchronously,
  whether a purchase is approved.
- **Inputs:** `cardId`, `merchantName`, `amount`, `currency`.
- **Outputs:** `201 Created`, always — body `status` is `APPROVED` or
  `DECLINED`. Never a 4xx/5xx for a legitimate decline; a 4xx means the
  request itself was invalid (e.g. unknown `cardId`).
- **Business rules, in order:**
  1. The card must exist (else `404`).
  2. The card must be `ACTIVE` (else `DECLINED`, reason "Card is not
     active").
  3. The purchase must fit under the card's daily limit, checked against
     everything `APPROVED` for that card since midnight UTC (else
     `DECLINED`, reason names the limit).
  4. Only then is a journal entry posted to `ledger-service` (see FR-6);
     a ledger rejection (insufficient funds) also produces `DECLINED`,
     with the ledger's own error text as the reason.
- **Acceptance criteria:**
  - A blocked/inactive card is declined without ever calling
    `ledger-service`.
  - A purchase over the daily limit is declined without ever calling
    `ledger-service`.
  - A purchase within the limit on an active card is handed to the
    ledger, and its outcome (approved/declined) is determined solely by
    the ledger's response.
- **Covered by:** `CardAuthorizationServiceTest`, `CardAuthorizationExecutionServiceTest`.
- **Traces to:** PR-4, PR-5.

### FR-5: Enforce the daily purchase limit

- **Description:** Every card carries a `dailyPurchaseLimit`. The running
  total of `APPROVED` authorizations for that card since midnight UTC,
  plus the new purchase amount, must not exceed it.
- **Inputs:** The card's `dailyPurchaseLimit`; the sum of today's approved
  authorizations for that card.
- **Outputs:** A `DECLINED` authorization when the projected total would
  exceed the limit.
- **Business rules:** The running total is recomputed from history on
  every request (never cached), the same "balance is derived, never
  stored" discipline `ledger-service` applies to account balances.
- **Acceptance criteria:** Two purchases that individually fit but
  together exceed the limit — the first is approved, the second declined.
- **Covered by:** `CardAuthorizationServiceTest`.
- **Traces to:** PR-5.

### FR-6: Post an approved purchase to the ledger

- **Description:** An approved purchase posts a single balanced journal
  entry: debit the cardholder's ledger account (reached via
  `accounts-service`), credit the card network's clearing ledger account
  for that currency (see ADR-0010).
- **Inputs:** The cardholder's `ledgerAccountId`, the clearing account's
  ledger account id, the purchase amount.
- **Outputs:** On success, `APPROVED` with `journalEntryReference` set. On
  a ledger rejection (insufficient funds) or ledger-service being
  unreachable, `DECLINED` with a reason instead of an exception.
- **Business rules:** Exactly the same two-legged, balanced-entry shape
  `internal-transfer-service` uses for a transfer — `ledger-service` is
  never modified or given cards-specific logic.
- **Acceptance criteria:** A successful ledger posting approves; a ledger
  exception declines instead of propagating; a ledger `{"error": "..."}`
  body is unwrapped into a clean decline reason.
- **Covered by:** `CardAuthorizationExecutionServiceTest`.
- **Traces to:** PR-6.

### FR-7: Authorization history

- **Description:** `GET /api/card-authorizations?cardId=` returns a
  card's authorizations, most recent first, approved and declined alike.
- **Inputs:** `cardId`.
- **Outputs:** A list, newest first.
- **Business rules:** None beyond ordering.
- **Acceptance criteria:** History includes both `APPROVED` and
  `DECLINED` entries.
- **Covered by:** Manual verification via the Cards page (see PR-8);
  no dedicated unit test beyond the repository query.
- **Traces to:** PR-7.

### FR-8: Demo UI

- **Description:** The web-banking Cards page lets a user issue a card
  against one of their accounts, activate or block it, and simulate a
  purchase, showing the approved/declined result and history inline.
- **Inputs:** UI form inputs only; all state comes from the two new
  services' APIs, never from local storage (unlike the account/customer
  "directory" convenience, cards are always fetched live).
- **Outputs:** Rendered card list, authorization result toast, and history
  table.
- **Acceptance criteria:** `npm run build`, `npm run lint`, and
  `npm test` all pass with the new page included.
- **Covered by:** Full frontend build + existing unit test suite (13
  tests) run clean with this page added; no browser/e2e test for this
  page specifically (`src/tests/e2e` remains scaffolding platform-wide).
- **Traces to:** PR-8.

## 4. Non-Functional Requirements

| Category | Requirement |
| --- | --- |
| Performance | Authorization decision is synchronous, single request/response — no polling, matching a real card-present authorization's latency expectation. |
| Security | No PAN, CVV, or PIN is ever stored — `cardNumberMasked` and `cardNumberLastFour` only. Not a substitute for PCI-DSS scope reduction in a real deployment; see `docs/domains/cards.md`. |
| Availability | Each new service owns its own Postgres database, following ADR-0002 — an outage in one does not take down the other or the existing slice. |
| Consistency | The ledger's own invariants (balanced entries, no negative balance) are the single source of truth for whether a debit succeeds; card-authorization-service never re-implements a balance check itself. |

## 5. Use Cases / User Flows

1. Customer opens an `ACTIVE` account (existing slice) → issues a card
   (`ISSUED`) → activates it (`ACTIVE`) → authorizes a $4.50 purchase →
   `APPROVED`, ledger posts $4.50 debit/credit → history shows it.
2. Same customer's card is reported lost → blocked (`BLOCKED`) → any
   further authorization attempt is `DECLINED` before the ledger is ever
   consulted.
3. Customer spends up to their daily limit across several approved
   purchases → the next purchase, even a small one, is `DECLINED` for
   exceeding the limit.
4. Customer attempts a purchase larger than their account balance on an
   otherwise-eligible `ACTIVE` card under its daily limit → `ledger-service`
   rejects the posting → `DECLINED`, reason surfaced from the ledger.

## 6. Data Requirements

- **`cards`** (card-management-service): id, account id, customer id,
  masked card number + last four, cardholder name, card type, expiry,
  status, daily purchase limit, timestamps for creation/activation/block.
- **`card_authorizations`** (card-authorization-service): id, card id,
  account id, merchant name, amount, currency, status, ledger journal
  entry reference (nullable), decline reason (nullable), created at.
- **`clearing_accounts`** (card-authorization-service): one row per
  currency, mapping it to the card network's ledger account id.

## 7. Interface Requirements

- REST APIs on `card-management-service` (`/api/cards/**`) and
  `card-authorization-service` (`/api/card-authorizations/**`), both
  routed through `gateways/api-gateway`.
- Outbound HTTP clients: `card-authorization-service` calls
  `card-management-service`, `accounts-service`, and `ledger-service`;
  `card-management-service` calls only `accounts-service`.
- New `CardsPage.tsx` in `apps/web-banking`, plus `api/cards.ts` and
  `api/cardAuthorizations.ts`.

## 8. Assumptions & Dependencies

- `accounts-service` and `ledger-service` are unmodified — every
  assumption this FRD makes about their behavior (balance derivation,
  the no-negative-balance guarantee, the `{"status","error","timestamp"}`
  error shape) is already covered by their own existing tests.
- Single-currency purchases only: a card's authorization currency is
  assumed to match its linked account's currency, the same simplification
  `internal-transfer-service` makes for cross-account transfers.

## 9. Traceability Matrix

| FRD ID | PRD ID |
| --- | --- |
| FR-1 | PR-1 |
| FR-2 | PR-2 |
| FR-3 | PR-3 |
| FR-4 | PR-4 |
| FR-5 | PR-5 |
| FR-6 | PR-6 |
| FR-7 | PR-7 |
| FR-8 | PR-8 |
