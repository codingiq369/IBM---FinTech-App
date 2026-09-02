# Environments

Four environments are configured for this platform: **dev**, **staging**,
**uat**, and **production**. Naming is not perfectly uniform across the
repo -- this directory and `config/` predate the rest of this setup and
already used the spelled-out `development`, so that folder/file name was
kept as-is rather than renamed; every *new* piece added for this setup
(Spring profiles, Kubernetes, Terraform, CI/CD) uses the short `dev`. Both
names refer to the same environment.

This directory holds each environment's docker-compose `.env` file -- the
values a human runs the stack with locally to reproduce that environment's
behavior. Everything else that makes an environment real lives next to the
code it configures, not here:

| What                                   | Where                                                                                    |
|-----------------------------------------|-------------------------------------------------------------------------------------------|
| Docker Compose values (ports, profile)  | `environments/development\|staging\|uat\|production/.env` (this directory)                 |
| Spring Boot behavior per env            | `application-dev\|staging\|uat\|production.yml` next to each service's `application.yml`   |
| Frontend build-time config              | `apps/web-banking/.env.development\|staging\|uat\|production`                               |
| Platform-wide config (flags, limits)    | `config/application/development\|staging\|uat\|production.yaml`, `config/feature-flags/...` |
| Kubernetes manifests                    | `infrastructure/kubernetes/overlays/dev\|staging\|uat\|production/`                        |
| Cloud infrastructure (Terraform)        | `infrastructure/terraform/environments/dev\|staging\|uat\|production/`                     |
| CI/CD deploy pipeline                   | `.github/workflows/cd-dev\|staging\|uat\|production.yml`                                   |

`environments/local/`, `environments/testing/`, and
`environments/disaster-recovery/` are separate, still-empty scaffolding --
not part of the four environments above. See each subdirectory's README.

## Promotion path

`dev` -> `staging` -> `uat` -> `production`. A change reaches an environment
only through the matching `.github/workflows/cd-<env>.yml` pipeline (built
image -> `kubectl apply -k infrastructure/kubernetes/overlays/<env>`); staging
deploys automatically off `main`, uat and production require an approved
GitHub Environment review. See `docs/operations/deployment.md`.
