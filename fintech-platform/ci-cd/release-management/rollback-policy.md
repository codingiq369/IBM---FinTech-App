# Rollback policy

Use `.github/workflows/rollback.yml` (manual `workflow_dispatch`, pick the
environment and the deployment) -- it runs `kubectl rollout undo`, which
reverts to the previous ReplicaSet for that Deployment. It does not revert
a database migration; Flyway migrations in this codebase are additive by
convention (see `docs/onboarding/database-development.md`), so a rollback
of the application should not require a matching schema rollback under
normal circumstances.

Who can trigger it: anyone with write access can dispatch the workflow, but
`uat` and `production` still run through their GitHub Environment's
required reviewers, the same gate a forward deploy goes through -- a
rollback is a deploy, and rollbacks to production should not be
approved-only-in-theory.

When to use it instead of rolling forward: if the failure is isolated to
the most recent deploy (a bad image, a bad config patch) and a fix isn't
already close to ready, roll back. If the failure is a data problem, or the
previous version has a since-fixed bug of its own, rolling forward with a
fix is usually faster than a rollback that just reintroduces the old bug.
