# development / backend

The 4 backend services and the API gateway don't have per-environment
*code* -- they have per-environment *configuration*. For `development`
that's:

- `SPRING_PROFILES_ACTIVE=dev`, which activates each service's
  `src/main/resources/application-dev.yml` (logging, actuator exposure,
  connection pool sizing, and -- for the gateway -- CORS).
- The image and replica/resource settings in
  `infrastructure/kubernetes/overlays/dev/` (short name -- see the note at
  the top of `environments/README.md`).

There's nothing to put in this folder itself; it exists to name the
concept. See `environments/development/.env` to run it locally.
