# production / backend

The 4 backend services and the API gateway don't have per-environment *code* -- they have per-environment *configuration*. For the `production` environment that's:

- `SPRING_PROFILES_ACTIVE=production`, which activates each service's `src/main/resources/application-production.yml` (logging, actuator exposure, connection pool sizing, and -- for the gateway -- CORS).
- The image and replica/resource settings in `infrastructure/kubernetes/overlays/production/`.

There's nothing to put in this folder itself; it exists to name the concept. See `environments/production/.env` to run it locally.
