# pipelines/databases

No separate database pipeline -- each service's own Flyway migrations (`src/main/resources/db/migration/`) run automatically on that service's startup, in every environment. `deployment/scripts/migrate.sh` creates each environment's per-service databases up front (RDS only auto-creates one `db_name` on first boot, same limitation `deployment/docker/postgres-init` works around locally). Provisioning the instance itself is `infrastructure/terraform/modules/databases`.
