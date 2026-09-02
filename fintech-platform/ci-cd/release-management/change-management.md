# Change management

`staging` deploys automatically on every merge to `main` -- no separate
approval, because staging is meant to be cheap to break and quick to
iterate on.

`uat` and `production` are gated by GitHub Environment required reviewers
(configured in the repo's Settings -> Environments -> `uat` / `production`,
not in this repo's files) -- a `workflow_dispatch` run against
`cd-uat.yml` or `cd-production.yml` pauses for an approval before the
`deploy` job starts. That approval, plus the Actions run history itself
(who dispatched it, what image tag or release tag, when), is the audit
trail for what changed in a regulated environment -- see
`docs/compliance/soc2.md` and `docs/compliance/audit-policy.md` for the
broader audit requirements this feeds into.

Emergency changes (an incident fix that can't wait for the normal
uat sign-off) still go through the same `cd-production.yml` dispatch and
the same required reviewers -- there is no separate emergency-bypass path
today. If that turns out to be too slow during a real incident, that's a
gap to close deliberately, not to work around ad hoc.
