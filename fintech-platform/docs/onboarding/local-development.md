# Local development

The fastest path is still the root README's quickstart -- clone, `cd
deployment/docker && docker compose up --build`, open localhost:3000. This
page covers the environment-aware variants of that.

## Running against a named environment's profile

```bash
cd deployment/docker
docker compose --env-file ../../environments/development/.env -f docker-compose.yml up --build
```

Swap `development` for `staging`, `uat`, or `production` to run the same
stack under that Spring profile and CORS configuration, on different host
ports so more than one can run at once (see `environments/<env>/.env` for
the exact port map, and `environments/README.md` for what each environment
actually differs in). `staging`/`uat`/`production` here are for reproducing
that profile's *behavior* locally -- they are not how the real shared
staging/uat/production environments run; that's
`infrastructure/kubernetes/overlays/<env>` on a real cluster.

## Running one service on its own

Unchanged from the root README: each service under `services/*/*-service`
and `ledger/general-ledger-service` is a standalone Maven project.

```bash
cd services/customer/customer-service
mvn spring-boot:run
```

To run it under a specific profile instead of the bare defaults:

```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

This picks up `application-dev.yml` in that service's
`src/main/resources/` on top of `application.yml`.

## Running the frontend on its own

```bash
cd apps/web-banking
npm install
npm run dev       # loads .env.development
```

`npm run dev` always uses `.env.development` (Vite's own default mode for
the dev server). To see what a staging/uat/production build looks like
locally: `npm run build:staging` (or `:uat`, `:production`), then `npm run
preview` to serve the built output.

## Feature flags and limits while developing

`config/feature-flags/development.yaml` has `allow-test-data-seeding: true`
and `verbose-error-responses: true` -- neither is read by any code yet (see
that file's header), but they're the target defaults for local work.
`config/limits/*.yaml`'s `environments.dev` entries are deliberately huge
(e.g. a $1,000,000 single-transfer limit) so limit-checking, once built,
doesn't get in the way of exercising the UI locally.
