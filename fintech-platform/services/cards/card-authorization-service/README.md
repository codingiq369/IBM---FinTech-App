# card-authorization-service

Authorizes a card purchase in real time and posts its money movement.
Consolidates what the fuller scaffold names as two services
(`card-authorization-service` and `card-transaction-service`) into one, the
same way `internal-transfer-service` both decides and executes a transfer —
see [`docs/architecture/vertical-slice.md`](../../../docs/architecture/vertical-slice.md).

## What a purchase authorization actually checks

1. The card ([`card-management-service`](../card-management-service)) must exist and be `ACTIVE`.
2. The purchase must fit under the card's daily purchase limit — checked
   against everything already `APPROVED` for that card since midnight UTC.
3. Only then does this service ask `ledger-service` to post a balanced
   journal entry: **debit** the cardholder's own ledger account (reached via
   `accounts-service`), **credit** the card network's *clearing account* —
   one ledger account per currency, owned by this service, standing in for
   a real merchant-acquiring/interchange settlement domain. Why a clearing
   account instead of building that domain: see
   [ADR-0010](../../../docs/architecture/architecture-decisions/ADR-0010-card-network-clearing-account.md).

A decline is a normal, recorded outcome (`{"status":"DECLINED","declineReason":"..."}`),
never an HTTP error — a real card network responds to *every* swipe, it
just doesn't always say yes. An HTTP 4xx here means the request itself was
malformed (e.g. no such card), not that a purchase was declined.

## API

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/card-authorizations` | Authorize a purchase: `{cardId, merchantName, amount, currency}`. Always 201; check `status`. |
| `GET` | `/api/card-authorizations/{id}` | Fetch a single authorization. |
| `GET` | `/api/card-authorizations?cardId=` | A card's authorization history, most recent first. |

## Running it

Requires a reachable Postgres (`card_authorization_db`), and
`card-management-service`, `accounts-service`, and `ledger-service` all
reachable at their respective `*_SERVICE_URL` env vars.

```bash
cd services/cards/card-authorization-service
mvn spring-boot:run
```

Or as part of the full stack: `cd deployment/docker && docker compose up --build`.

## Testing

```bash
mvn test
```

`CardAuthorizationServiceTest` proves a blocked/inactive card and a
limit-exceeding purchase are both declined *without ever calling
ledger-service*. `CardAuthorizationExecutionServiceTest` proves a
successful ledger posting approves the authorization, a ledger failure
declines it instead of throwing, and the ledger's `{"error": "..."}` body
is unwrapped into a clean decline reason — the same three guarantees
`TransferExecutionServiceTest` proves for transfers.
