# Production access

There is no production environment running anywhere today -- this
describes how access is meant to work once
`infrastructure/terraform/environments/production` is actually applied
against a real AWS account, so the model is decided deliberately rather
than improvised the first time someone needs it.

## Who can deploy

Nobody deploys to production directly. `.github/workflows/cd-production.yml`
is the only path in, triggered by a manual `workflow_dispatch` naming a
release tag. That job runs under the `production` GitHub Environment,
which is configured (in the repository's Settings -> Environments, not in
this codebase) with required reviewers -- the dispatch pauses until one of
them approves it. See `ci-cd/release-management/change-management.md`.

## Who can access the cluster/database directly

Not covered by this pass -- `security/authentication` and
`security/authorization` are still empty scaffolding (see "No auth" in
`docs/architecture/vertical-slice.md`), and human `kubectl`/`psql` access
to the production cluster and database is a separate access-control problem
from the deploy-pipeline access controlled above. Until that's built, the
working assumption is: nobody gets standing direct access; break-glass
access (an incident) goes through whatever your org's existing AWS IAM /
EKS access entry process is, logged the same way any other production
access would be, per `docs/compliance/soc2.md`.

## Secrets

No person ever holds the production database password. It's generated and
passed to Terraform as `TF_VAR_db_master_password` from
`.github/workflows/infrastructure.yml`'s secret store (`secrets.DB_MASTER_PASSWORD`
in that workflow), stored in Secrets Manager by the `secrets` Terraform
module, and synced into the cluster's `fintech-db-credentials` Kubernetes
Secret -- see
`docs/architecture/architecture-decisions/ADR-0006-secrets-management.md`.
`environments/production/.env`'s password is explicitly a local-only
placeholder for reproducing the production Spring profile with Docker
Compose and must never be reused anywhere real (see that file's own
header comment).

## IAM roles

Each of the 6 workloads gets its own IAM role (IRSA), scoped to only that
workload's own Kubernetes ServiceAccount and only the permissions it
actually needs (today: read the DB credentials secret, write logs) -- see
`infrastructure/terraform/modules/iam`. There is no shared "the app" role
with broad permissions.
