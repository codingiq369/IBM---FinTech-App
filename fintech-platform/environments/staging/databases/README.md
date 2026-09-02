# staging / databases

Each backend service owns its database (`customer_db`, `accounts_db`, `ledger_db`, `transfers_db`) via Flyway migrations under its own `src/main/resources/db/migration/`. For `staging` those run against:

- **Locally**, the `postgres` container in `deployment/docker/docker-compose.yml`, using `environments/staging/.env` for credentials and port.
- **On the cluster**, the RDS instance provisioned by `infrastructure/terraform/environments/staging/` (see the `databases` module) -- connection details are injected via the Kubernetes Secret in `infrastructure/kubernetes/overlays/staging/`.

There is no environment-specific SQL here on purpose: the same migrations run everywhere, so schema drift between environments can't happen.
