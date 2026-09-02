# environments/test

Not one of the four environments this setup covers (dev, staging, uat,
production -- see the sibling directories). Left as scaffolding; would
follow the `environments/dev` pattern if this project needs a dedicated,
disposable cluster+database per CI run rather than the shared `dev`
environment. The `testing` Spring profile used by CI today
(config/application/testing.yaml) doesn't need its own cloud infrastructure
-- it runs against ephemeral containers in the CI runner.
