# development / configuration

Cross-cutting configuration for `development` lives in
`config/application/development.yaml` (platform defaults) and
`config/feature-flags/development.yaml` (what's turned on). Compliance rule
overrides for this environment are in `config/compliance/*.yaml` under
each file's `environments.dev` key -- the compliance files key by the
short `dev`, like everything added in this pass outside `environments/`
and `config/application|feature-flags/` itself; see the note at the top of
`environments/README.md`.
