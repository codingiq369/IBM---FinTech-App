# Domain: Cards

## What's built

Two services, both real and runnable — see
[`docs/architecture/vertical-slice.md`](../architecture/vertical-slice.md#the-card-authorization-flow-end-to-end)
for the full flow and sequence diagram, and the product documents for why:
[PRD](../product/prd-card-issuance-and-authorization.md) ·
[FRD](../product/frd-card-issuance-and-authorization.md) ·
[ADR-0010](../architecture/architecture-decisions/ADR-0010-card-network-clearing-account.md).

| Service | Owns |
| --- | --- |
| [`card-management-service`](../../services/cards/card-management-service) | Card identity and lifecycle: issuance, activation, blocking. |
| [`card-authorization-service`](../../services/cards/card-authorization-service) | The real-time approve/decline decision for a purchase, and posting its ledger movement. |

Only `DEBIT` cards are issuable today, linked one-to-one with a bank
account opened through `account-service`. A card authorizes a purchase by
debiting its linked account's real ledger balance — there is no separate
"card balance" anywhere in this platform, by design: the ledger stays the
single source of truth for money, the same way it is for transfers.

## What's still scaffolding

Everything else under `services/cards/` remains an empty placeholder,
matching the rest of this repository's scaffold-vs-built split:

| Folder | Would eventually own |
| --- | --- |
| `card-limits-service` | Per-merchant-category or velocity limits beyond the flat daily limit `card-management-service` enforces today. |
| `card-dispute-service` | Chargebacks and dispute case management. |
| `card-rewards-service` | Points/cashback accrual on approved purchases. |
| `card-tokenization-service` | A real, PCI-DSS-scoped PAN vault — see "On PCI-DSS scope" below. |

`docs/product/card-issuance-backlog.md` tracks each of these as a
scoped-but-not-started or intentionally-deferred backlog item, not an
open question.

## Key design decisions

- **A card purchase settles against a card-network clearing ledger
  account**, one per currency, rather than a real merchant-acquiring
  domain — see ADR-0010 for the full reasoning. This is the one
  cards-specific concept that exists anywhere in this platform; everything
  else reuses `account-service` and `ledger-service` completely unmodified.
- **The authorization decision is layered, cheapest checks first**: card
  status, then the daily limit, then (only if both pass) the ledger's own
  balance check. A blocked card or one over its limit never reaches
  `ledger-service` at all — see `CardAuthorizationService`.
- **A decline is not an error.** `POST /api/card-authorizations` returns
  `201` whether the outcome is `APPROVED` or `DECLINED`; an HTTP 4xx means
  the *request* was malformed (e.g. an unknown card), never that a
  purchase was declined. This mirrors how `internal-transfer-service`
  treats a `FAILED` transfer as a successfully recorded outcome, not a
  thrown exception.

## On PCI-DSS scope

This platform stores no PAN, CVV, or PIN anywhere — `card-management-service`
generates a synthetic masked number (`cardNumberMasked` /
`cardNumberLastFour`) purely for the demo UI to display, the same way
`AccountNumberGenerator` fabricates a bank account number. That is **not**
a substitute for real card data handling: a production issuer would never
generate or store even a synthetic PAN outside a PCI-DSS Level 1 scoped
vault (`card-tokenization-service`'s eventual job), with every service in
this repository — including `card-authorization-service` — kept entirely
out of that scope by only ever handling a token, never a real card
number. See `docs/compliance/pci-dss.md`.
