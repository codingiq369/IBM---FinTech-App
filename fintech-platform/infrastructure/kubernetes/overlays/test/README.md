# overlays/test

Not one of the four environments this setup covers (dev, staging, uat,
production -- see infrastructure/kubernetes/overlays/{dev,staging,uat,production}
and the root of this repo's `environments/` directory). Left as scaffolding;
a `test` overlay would follow the same pattern as `overlays/dev` if this
project ever needs a dedicated ephemeral cluster namespace for integration
tests, distinct from the `testing` Spring profile used by CI today (see
config/application/testing.yaml).
