# Deployment architecture

Each of the 6 real components (customer-service, accounts-service,
ledger-service, transfers-service, api-gateway, web-banking) is one
container image, one Kubernetes Deployment, one Service. There's no shared
"app" deployable -- each has its own image, its own rollout, its own
resource limits, so one service's bad deploy doesn't force redeploying
everything else.

```
                        Internet
                           |
                      [ Ingress ]  (ACM cert + WAF via infrastructure/terraform/modules/load-balancer)
                       /        \
                 /api/*         /*
                  |               |
           [ api-gateway ]   [ web-banking ]  (static, built with VITE_API_BASE_URL baked in)
                  |
     -------------+---------------+---------------+
     |            |               |               |
[customer-  [accounts-      [ledger-        [transfers-
 service]    service]        service]        service]
     |            |     \        |          /     |
     |            |      \       |         /       |
     +----------- | ------+------+--------+ -------+
                  |  (each has its own database on
                  |   one shared RDS instance)
             [ RDS Postgres ]
```

Same shape in every environment; what changes per environment (dev /
staging / uat / production) is sizing (replica counts, resource
requests/limits -- see `infrastructure/kubernetes/overlays/<env>`), the
database instance class and Multi-AZ setting (see
`infrastructure/terraform/environments/<env>`), and the CORS/ingress
hostnames. Locally, the same shape runs via Docker Compose
(`deployment/docker/docker-compose.yml`) with Postgres as a plain container
instead of RDS.

## Why one shared Postgres instance, not one per service

Each service owns its own database (`customer_db`, `accounts_db`,
`ledger_db`, `transfers_db`) for real logical isolation -- no service reads
another's tables -- but they share one RDS instance rather than four, to
keep infrastructure cost and operational surface down for a reference
architecture. See
`docs/architecture/architecture-decisions/ADR-0002-database-per-service.md`
for the fuller reasoning and what a real system might do differently at
scale.

## Why Kustomize, not Helm

`infrastructure/kubernetes/base` + `overlays/<env>` is the maintained
deployment mechanism. `infrastructure/kubernetes/helm/*` exists in the
scaffold but describes different (non-existent, Node.js) services and was
left as scaffolding rather than built out to avoid two competing
mechanisms for the same 6 real components -- see that directory's own
READMEs.

## Why the frontend's config is baked in, not runtime

`apps/web-banking` is a static single-page app served by nginx -- there's
no process to hand it environment variables at container start the way the
Java services get `DB_URL` etc. `VITE_API_BASE_URL` has to be set at image
*build* time (see the comment in `apps/web-banking/Dockerfile`), which is
why `.github/workflows/build.yml`'s `frontend-image` job takes it as an
input and why each environment gets its own frontend image, not one image
reused everywhere with different runtime config.
