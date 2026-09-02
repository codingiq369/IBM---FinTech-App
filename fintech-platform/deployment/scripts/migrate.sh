#!/usr/bin/env bash
# Creates the four per-service databases on a Postgres instance, then lets
# each service's own Flyway migrations run on its next startup.
#
# deployment/docker/postgres-init/001-create-databases.sql does this
# automatically for the docker-compose Postgres container (it only runs
# once, on first volume init). RDS has the same "only creates the one
# db_name given at creation" limitation and no equivalent init-script hook,
# so this script is that same step, run once against a real instance after
# infrastructure/terraform/modules/databases provisions it.
#
# Usage: migrate.sh <host> <port> <username> <database-with-permissions>
#   PGPASSWORD must be set in the environment (never pass it as an argument).
#
# Example, against an environment's RDS instance (endpoint from
# `terraform output db_endpoint` in infrastructure/terraform/environments/<env>):
#   PGPASSWORD=... deployment/scripts/migrate.sh fintech-staging.xxxxx.rds.amazonaws.com 5432 fintech fintech
set -euo pipefail

HOST="${1:?usage: migrate.sh <host> <port> <username> <database>}"
PORT="${2:?usage: migrate.sh <host> <port> <username> <database>}"
USERNAME="${3:?usage: migrate.sh <host> <port> <username> <database>}"
ADMIN_DB="${4:?usage: migrate.sh <host> <port> <username> <database>}"

if [ -z "${PGPASSWORD:-}" ]; then
  echo "set PGPASSWORD in the environment first" >&2
  exit 1
fi
command -v psql >/dev/null 2>&1 || { echo "psql is required" >&2; exit 1; }

for db in customer_db accounts_db ledger_db transfers_db; do
  echo "==> Ensuring database '$db' exists on $HOST:$PORT"
  # CREATE DATABASE has no IF NOT EXISTS, and can't run inside a transaction
  # block, hence the plain conditional shell check instead of a DO block.
  EXISTS="$(psql -h "$HOST" -p "$PORT" -U "$USERNAME" -d "$ADMIN_DB" -tAc "SELECT 1 FROM pg_database WHERE datname = '$db'")"
  if [ "$EXISTS" = "1" ]; then
    echo "    already exists, skipping"
  else
    psql -h "$HOST" -p "$PORT" -U "$USERNAME" -d "$ADMIN_DB" -c "CREATE DATABASE $db"
    echo "    created"
  fi
done

echo "==> Done. Each service creates its own tables via Flyway (src/main/resources/db/migration/) on next startup."
