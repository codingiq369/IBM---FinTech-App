# pipelines/backend

The 5 Java services' CI/CD runs through `.github/workflows/unit-tests.yml` (the `backend` matrix job: mvn test per module) and `.github/workflows/build.yml` (the `backend-images` matrix job). See `ci-cd/github-actions/unit-tests.yml` and `build.yml` for the call graph.
