# staging / frontend

`apps/web-banking` reads `VITE_API_BASE_URL` and `VITE_ENV_NAME` from `apps/web-banking/.env.staging`, baked in at build time (`npm run build:staging`). The Docker build passes the same value through as a build arg -- see `VITE_API_BASE_URL` in `environments/staging/.env` and the `web-banking` service's `build.args` in `deployment/docker/docker-compose.yml`.
