# pipelines/frontend

`apps/web-banking`'s CI/CD runs through `.github/workflows/unit-tests.yml` (the `frontend` job: npm test) and `.github/workflows/build.yml` (the `frontend-image` job, which bakes in `VITE_API_BASE_URL` per environment). See `ci-cd/github-actions/unit-tests.yml` and `build.yml`.
