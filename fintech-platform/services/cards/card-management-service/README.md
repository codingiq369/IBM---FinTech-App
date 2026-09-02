# card-management-service

Issues and tracks debit cards linked to a bank account. Owns "which cards
exist, who they belong to, and their lifecycle status" — nothing about
whether a given purchase is approved (that's
[`card-authorization-service`](../card-authorization-service)) and nothing
about balances (that's `ledger-service`, reached indirectly through
`accounts-service`).

Part of the second vertical slice built on top of the platform's
[core vertical slice](../../../docs/architecture/vertical-slice.md)
(onboarding → account → transfer). See
[`docs/product/prd-card-issuance-and-authorization.md`](../../../docs/product/prd-card-issuance-and-authorization.md)
for why this exists and
[ADR-0010](../../../docs/architecture/architecture-decisions/ADR-0010-card-network-clearing-account.md)
for the key design decision behind the pair of services.

## API

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/cards` | Issue a new DEBIT card against an ACTIVE account. Returns the card in `ISSUED` status. |
| `POST` | `/api/cards/{id}/activate` | `ISSUED` → `ACTIVE`. A card cannot authorize a purchase until this has been called. |
| `POST` | `/api/cards/{id}/block` | `ISSUED`/`ACTIVE` → `BLOCKED`. Simulates a lost/stolen-card report; irreversible in this slice. |
| `GET` | `/api/cards/{id}` | Fetch a single card. |
| `GET` | `/api/cards?accountId=` or `?customerId=` | List cards for an account or a customer. |

Every error response is `{"status", "error", "timestamp"}` (see
`GlobalExceptionHandler`), matching every other service in this repository.

## Running it

Requires a reachable Postgres (`card_management_db`, created automatically
by `deployment/docker/postgres-init/001-create-databases.sql` when running
the full stack) and `accounts-service` reachable at `ACCOUNTS_SERVICE_URL`.

```bash
cd services/cards/card-management-service
mvn spring-boot:run
```

Or as part of the full stack: `cd deployment/docker && docker compose up --build`.

## Testing

```bash
mvn test
```

`CardServiceTest` proves a card can only be issued against an ACTIVE
account, and that accounts-service is never called for downstream side
effects when it isn't — the same "validate before you act" discipline
`AccountServiceTest` proves for account opening.
