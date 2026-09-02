# Deployment

Four environments: **dev**, **staging**, **uat**, **production**. Promotion
flows one way, dev -> staging -> uat -> production, and each stage is a
GitHub Actions pipeline, never a person running `kubectl apply` by hand
against a shared environment.

## What's environment-specific

| Layer | Where |
|---|---|
| Spring Boot behavior (logging, actuator exposure, CORS, pool sizing) | `application-<env>.yml` next to each service's `application.yml` |
| Frontend build-time config | `apps/web-banking/.env.<mode>` |
| Docker Compose values for running an environment locally | `environments/<env>/.env` |
| Platform-wide config and feature flags | `config/application/<env>.yaml`, `config/feature-flags/<env>.yaml` |
| Kubernetes manifests | `infrastructure/kubernetes/base/` (shared) + `overlays/<env>/` (per-env patches) |
| Cloud infrastructure | `infrastructure/terraform/modules/` (shared) + `environments/<env>/` (per-env sizing) |

See `environments/README.md` for the full map.

## How a change actually reaches an environment

1. A PR merges to `main`. `.github/workflows/ci.yml` has already run unit
   tests, a build-only image build, and an integration test against the
   vertical slice.
2. `.github/workflows/cd-dev.yml` builds real images (tagged `dev-<sha>`)
   and deploys them to the `fintech-dev` namespace automatically.
3. `.github/workflows/cd-staging.yml` picks up wherever `cd-dev` just
   succeeded and does the same into `fintech-staging` -- also automatic.
4. Promoting to **uat** is a manual `workflow_dispatch` of
   `.github/workflows/cd-uat.yml`, naming the exact `staging-<sha>` image to
   promote. The `uat` GitHub Environment's required reviewers gate this.
5. Promoting to **production** is a manual `workflow_dispatch` of
   `.github/workflows/cd-production.yml` against a cut release tag
   (`vX.Y.Z`), not a branch commit. The `production` GitHub Environment's
   required reviewers gate this too.

Each `cd-<env>.yml` deploy runs `kustomize edit set image` against
`infrastructure/kubernetes/overlays/<env>` and `kubectl apply -k`. See
`ci-cd/release-management/versioning.md` for the tagging scheme and
`release-checklist.md` for what should be true before each promotion.

## Running an environment yourself

Every environment can be reproduced locally with Docker Compose, without a
cluster:

```bash
cd deployment/docker
docker compose --env-file ../../environments/<env>/.env -f docker-compose.yml up --build
```

`docker compose up --build` with no `--env-file` (the root README's
quickstart) behaves exactly like `environments/development/.env` -- that
file just makes the choice explicit and nameable.

Deploying for real, against a cluster, uses `deployment/scripts/deploy.sh`
(the same steps `deploy-<env>.yml` runs in CI, for when you need to run
them by hand) and `deployment/scripts/health-check.sh` to verify the
result. `deployment/scripts/migrate.sh` is a one-time step per environment,
run once its RDS instance exists (`infrastructure/terraform/environments/<env>`)
and before its services first start, to create the four per-service
databases the same way `deployment/docker/postgres-init` does locally.

## What's not built yet

None of this has been run against real AWS credentials or a real cluster --
see each Terraform environment's and Kubernetes overlay's own README for
that caveat. `helm`, `kustomize` (the top-level `deployment/kustomize`,
distinct from the Kustomize setup under `infrastructure/kubernetes`),
`manifests`, and `release/{blue-green,canary,rolling}` under `deployment/`
are separate, still-empty alternative deployment mechanisms this pass
didn't build out -- `infrastructure/kubernetes` is the maintained path.
