# Backlog — Card Issuance & Authorization

Engineering ticket backlog for the increment described in
[`prd-card-issuance-and-authorization.md`](prd-card-issuance-and-authorization.md)
and [`frd-card-issuance-and-authorization.md`](frd-card-issuance-and-authorization.md).

> **A note on format.** `templates/company-tickets-template.json` in this
> repository is a different thing: it's the import schema for OFFICE iQ's
> own candidate-simulation catalog (workspace/company/ticket records used
> to train people on a 30-day simulated job), not a general-purpose
> engineering backlog format — its `tickets[]` are simulation days, capped
> at 30, with fields like `businessDomain` and `catalogType` that don't
> mean anything for fintech-platform's own engineering work. Using it here
> would misuse someone else's import contract, so this backlog is instead
> a plain engineering ticket table: epic → story → status, the shape any
> issue tracker (Jira, Linear, GitHub Issues) would import directly. The
> BRD/PRD/FRD templates *are* general-purpose project documents, per
> `templates/README.md`, which is why the PRD/FRD above do use them.

## Epic: Card issuance & lifecycle (card-management-service)

| ID | Story | Priority | Points | Status | Acceptance criteria |
| --- | --- | --- | --- | --- | --- |
| CARD-1 | Issue a DEBIT card against an active account | Must | 3 | **Done** | `CardServiceTest` |
| CARD-2 | Activate an issued card | Must | 1 | **Done** | `CardServiceTest` |
| CARD-3 | Block a card | Must | 1 | **Done** | `CardServiceTest` |
| CARD-4 | List cards by account or customer | Should | 1 | **Done** | `CardController` |
| CARD-5 | Unblock a previously blocked card | Could | 2 | To Do | New `CardStatus` transition; PRD PR-9 |
| CARD-6 | Physical card fulfillment/shipping status | Won't (this release) | 5 | Backlog | Needs a shipping/logistics integration |
| CARD-7 | Card replacement on block (issue a new card, same account, void the old number) | Could | 3 | To Do | Depends on CARD-5 being resolved either way |

## Epic: Real-time purchase authorization (card-authorization-service)

| ID | Story | Priority | Points | Status | Acceptance criteria |
| --- | --- | --- | --- | --- | --- |
| CARD-8 | Authorize a purchase: card-active check | Must | 2 | **Done** | `CardAuthorizationServiceTest` |
| CARD-9 | Authorize a purchase: daily limit check | Must | 3 | **Done** | `CardAuthorizationServiceTest` |
| CARD-10 | Authorize a purchase: post balanced journal entry on approval | Must | 5 | **Done** | `CardAuthorizationExecutionServiceTest` |
| CARD-11 | Card-network clearing account, lazily created per currency | Must | 3 | **Done** | `ClearingAccountServiceTest`, ADR-0010 |
| CARD-12 | Authorization history by card | Should | 1 | **Done** | `CardAuthorizationController` |
| CARD-13 | Per-merchant velocity / fraud scoring | Could | 8 | Backlog | Out of scope this release — see PRD Non-Goals |
| CARD-14 | Hold vs. settlement split (auth now, capture later) | Could | 8 | Backlog | Real card networks separate these; this release settles immediately |

## Epic: Platform wiring

| ID | Story | Priority | Points | Status | Acceptance criteria |
| --- | --- | --- | --- | --- | --- |
| CARD-15 | Route `/api/cards/**` and `/api/card-authorizations/**` through api-gateway | Must | 1 | **Done** | `gateways/api-gateway/src/main/resources/application.yml` |
| CARD-16 | Wire both services + databases into docker-compose | Must | 2 | **Done** | `deployment/docker/docker-compose.yml` |
| CARD-17 | Wire both services' host ports into all four environment `.env` files | Must | 1 | **Done** | `environments/*/.env` |
| CARD-18 | Cards page in web-banking: issue, activate, block, simulate a purchase | Should | 5 | **Done** | `apps/web-banking/src/pages/cards/CardsPage.tsx`; `npm run build`/`lint`/`test` all pass |
| CARD-19 | Kubernetes overlays / Terraform for the two new services | Should | 5 | To Do | Extend `infrastructure/kubernetes` and `infrastructure/terraform` the way `docs/operations/deployment.md` describes for the existing five services |
| CARD-20 | CI pipeline coverage for the two new services (`ci-cd/`) | Should | 3 | To Do | Mirror whatever pipeline definition covers `account-service` today |

## Epic: Beyond this release (deferred by the PRD's Non-Goals)

| ID | Story | Priority | Points | Status | Notes |
| --- | --- | --- | --- | --- | --- |
| CARD-21 | Credit cards (credit line, repayment) | Won't (this release) | 13 | Backlog | Needs `services/lending`; PRD PR-10 |
| CARD-22 | Card disputes / chargebacks | Won't (this release) | 8 | Backlog | `services/cards/card-dispute-service` remains scaffolding |
| CARD-23 | Card rewards | Won't (this release) | 5 | Backlog | `services/cards/card-rewards-service` remains scaffolding |
| CARD-24 | PCI-DSS-scoped tokenization vault for a real PAN | Won't (this release) | 13 | Backlog | `services/cards/card-tokenization-service` remains scaffolding; see `docs/domains/cards.md` |
| CARD-25 | Real merchant-acquiring / interchange settlement, replacing the ADR-0010 clearing account | Won't (this release) | 21 | Backlog | See ADR-0010's "Alternatives considered" |

**Status legend:** *Done* = merged and tested in this increment. *To Do* =
scoped, not started. *Backlog* = intentionally deferred, not yet scoped in
detail. *Won't (this release)* = explicitly out of scope per the PRD.
