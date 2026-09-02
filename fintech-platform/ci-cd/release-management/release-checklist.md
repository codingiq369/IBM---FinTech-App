# Release checklist

Before promoting a build to the next environment:

**dev -> staging** (automatic, see `.github/workflows/cd-staging.yml`):
`ci-cd/quality-gates/unit-test-gate.yml` passed on `main`.

**staging -> uat** (manual, `cd-uat.yml`):
unit tests and the staging deploy's health check both passed; no open
CRITICAL/HIGH findings from `security-scan.yml` against the staging image
(`ci-cd/quality-gates/security-gate.yml`); a person has exercised the
onboarding -> open account -> transfer flow against staging directly.

**uat -> production** (manual, `cd-production.yml`):
uat sign-off recorded (see `change-management.md`); a release tag cut on
`main`; `ci-cd/quality-gates/compliance-gate.yml`'s `production` bar met for
anything that changed; a rollback plan identified (see
`rollback-policy.md`) before the deploy starts, not after something breaks.

None of the coverage or compliance gates above are mechanically enforced
yet (see each file in `ci-cd/quality-gates/`) -- treat this list as the
manual checklist until they are.
