# overlays/staging

Deploys the base manifests into the `fintech-staging` namespace, sized for
staging: 2 replica(s) per workload, resource requests/limits
scaled accordingly, and this environment's DB host / CORS origin / ingress
host patched onto the shared base config.

Apply with (requires a real cluster and kubectl context -- this has not been
run through `kustomize build` or `kubectl apply --dry-run`, only validated as
YAML; treat any error you hit running it for real as a real bug to fix, the
same caveat the root README gives the Java services):

    kubectl apply -k infrastructure/kubernetes/overlays/staging

`DB_HOST` and the ServiceAccount role ARNs are placeholders
(`REPLACE_WITH_...`) until wired to the real outputs of
`infrastructure/terraform/environments/staging` -- see that directory's README.
The `.github/workflows/cd-staging.yml` pipeline is what actually runs this in
practice, after setting the real image tag with `kustomize edit set image`.
