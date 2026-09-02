# FinTech Platform

A reference architecture for a digital banking platform: microservices for
core banking, payments, lending, trading, fraud, and more, plus the
supporting data, ML, security, and infrastructure layers a real bank needs.

Most of this repository is a **scaffold** — the folder structure a platform
like this would eventually have, with the code still to be written. Two
real, runnable paths have been built through it so far:

## What's implemented

### Slice 1: onboarding → open account → transfer

A customer can be onboarded, open a bank account, and transfer money to
another account, with every balance backed by a real double-entry ledger.

- `services/customer/customer-service` — customer identity and KYC status
- `services/accounts/account-service` — opens and tracks bank accounts
- `ledger/general-ledger-service` — the general ledger; enforces that every
  debit has a matching credit
- `services/transfers/internal-transfer-service` — orchestrates moving
  money between two accounts

### Slice 2: issue a debit card → authorize a purchase

Built on top of slice 1 without changing it: a customer with an account
can be issued a debit card and use it to make a purchase, approved or
declined in real time against the same ledger. See
[`docs/product/prd-card-issuance-and-authorization.md`](docs/product/prd-card-issuance-and-authorization.md)
for why, and [ADR-0010](docs/architecture/architecture-decisions/ADR-0010-card-network-clearing-account.md)
for the one new architectural decision it needed.

- `services/cards/card-management-service` — issues cards, tracks
  ISSUED/ACTIVE/BLOCKED status
- `services/cards/card-authorization-service` — checks the card and its
  daily limit, then posts the purchase to the ledger

### Shared by both slices

- `gateways/api-gateway` — single entry point + CORS for the browser UI
- `apps/web-banking` — Vite + React + TypeScript demo UI, seven services,
  one Postgres instance (one database per service)

Read [`docs/architecture/vertical-slice.md`](docs/architecture/vertical-slice.md)
for how both fit together, sequence diagrams for the transfer and card
authorization flows, and what was simplified on purpose.

### Running it

Requires Docker.

```bash
cd deployment/docker
docker compose up --build
```

Then open **http://localhost:3000**. First boot takes a minute or two while
seven JVMs start and run their database migrations, and the frontend builds —
if the page says it can't reach the API gateway, or an early transfer fails
with an "upstream service unreachable" error, wait a bit and retry.

To run a single backend service on its own (e.g. for `mvn test` or debugging
in an IDE), each one under `services/*/*-service` and
`ledger/general-ledger-service` is a completely standalone Maven project —
`cd services/customer/customer-service && mvn spring-boot:run` works on its
own, as long as a Postgres instance is reachable at the `DB_URL` in its
`application.yml` (default: `localhost:5432`).

To run the frontend on its own against locally-running backend services:

```bash
cd apps/web-banking
npm install
npm run dev
```

### Running a named environment

The same stack can be run shaped like dev, staging, uat, or production --
its Spring profile, CORS origin, and (offset, so more than one can run at
once) host ports:

```bash
cd deployment/docker
docker compose --env-file ../../environments/staging/.env -f docker-compose.yml up --build
```

See `docs/operations/deployment.md` for the full four-environment setup
(Kubernetes overlays, Terraform, CI/CD pipelines per environment) and
`environments/README.md` for where each piece of it lives.

### Running the tests

```bash
cd services/customer/customer-service && mvn test
cd services/accounts/account-service && mvn test
cd services/transfers/internal-transfer-service && mvn test
cd ledger/general-ledger-service && mvn test
cd services/cards/card-management-service && mvn test
cd services/cards/card-authorization-service && mvn test
cd gateways/api-gateway && mvn test
cd apps/web-banking && npm test
```

The most important test in the repository is
[`JournalEntryTest`](ledger/general-ledger-service/src/test/java/com/fintechplatform/ledger/domain/JournalEntryTest.java):
it proves the ledger cannot construct an unbalanced journal entry, no
matter what a caller sends it.

> **Note on this build:** the backend Java services — including
> `card-management-service` and `card-authorization-service` — were
> written in an environment without access to Maven Central, so they
> could not be compiled or test-run before being handed off — they're
> written carefully and reviewed by hand, but run `mvn clean verify` (or
> `docker compose build`) yourself as a first step and treat any compiler
> error you hit as a real bug report. The frontend, by contrast, *was*
> built, installed, linted, and tested in an environment with registry
> access (npm's registry wasn't blocked the way Maven Central was) —
> including the new Cards page — so `apps/web-banking` should build,
> lint, and test clean as-is.

## Everything else

The rest of the top-level folders (`services/lending`, `services/trading`,
`ml-platform`, `data-platform`, `security`, and so on), plus the sibling
microservice folders next to the ones implemented here (e.g.
`services/accounts/account-opening-service`,
`services/transfers/wire-transfer-service`,
`services/cards/card-dispute-service`) and the unused pages in
`apps/web-banking/src/pages` (loans, investments, etc.) are still empty
scaffolding — placeholders for where that code will live. See
`docs/architecture/vertical-slice.md`'s "What to build next" section for a
suggested order to keep extending this, and
`docs/product/card-issuance-backlog.md` for what's specifically left
within the cards domain.

`infrastructure` is a partial exception: the Terraform and Kubernetes
needed to run *this* vertical slice across dev/staging/uat/production is
real (see `docs/operations/deployment.md`), but it hasn't been applied
against a real cluster or AWS account, and `infrastructure/service-mesh`,
`infrastructure/networking`, and most of `infrastructure/cloud` are still
empty scaffolding like everything else here.
