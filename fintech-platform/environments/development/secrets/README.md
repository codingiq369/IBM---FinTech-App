# development / secrets

No real secrets are ever committed here. `environments/development/.env`
holds only local-only, throwaway credentials for running docker-compose on
a laptop. Real `dev` credentials come from the Kubernetes Secret in
`infrastructure/kubernetes/overlays/dev/` (base template at
`infrastructure/kubernetes/base/secrets/`; short name -- see the note at
the top of `environments/README.md`), sourced from the secrets manager
described in
`docs/architecture/architecture-decisions/ADR-0006-secrets-management.md`.
