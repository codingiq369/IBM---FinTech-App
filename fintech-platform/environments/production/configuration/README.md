# production / configuration

Cross-cutting configuration for `production` lives in `config/application/production.yaml` (platform defaults) and `config/feature-flags/production.yaml` (what's turned on). Compliance rule overrides for this environment are in `config/compliance/*.yaml` under each file's `environments.production` key.
