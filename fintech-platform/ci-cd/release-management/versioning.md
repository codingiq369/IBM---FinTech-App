# Versioning

Every image built by `.github/workflows/build.yml` is tagged twice: once
with the environment-scoped tag the caller passes in (`dev-<sha>`,
`staging-<sha>`), and once with the raw commit SHA, so any image can be
traced back to an exact commit regardless of which environment tag it was
promoted under.

`dev` and `staging` deploy straight off `main` -- their tags are always
`<env>-<git-sha>`, and there's no independent versioning concept below
staging.

`uat` and `production` deploy from an explicit tag instead of a branch
commit:

- `uat` takes an already-built `staging-<sha>` image tag as input to
  `cd-uat.yml` -- it doesn't rebuild, it promotes the exact artifact that
  passed staging.
- `production` takes a semantic version tag (`vMAJOR.MINOR.PATCH`, e.g.
  `v1.4.0`) as input to `cd-production.yml`, which triggers `build.yml`
  to build fresh images tagged with that release version. Cut a release
  tag on `main` once uat sign-off is complete; that tag is what
  `cd-production.yml` should be given.
