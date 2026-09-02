# uat / backend

The 4 backend services and the API gateway don't have per-environment *code* -- they have per-environment *configuration*. For the `uat` environment that's:

- `SPRING_PROFILES_ACTIVE=uat`, which activates each service's `src/main/resources/application-uat.yml` (logging, actuator exposure, connection pool sizing, and -- for the gateway -- CORS).
- The image and replica/resource settings in `infrastructure/kubernetes/overlays/uat/`.

There's nothing to put in this folder itself; it exists to name the concept. See `environments/uat/.env` to run it locally.
