# overlays/dev

Deploys the base manifests into the `fintech-dev` namespace, sized for
dev: 1 replica(s) per workload, resource requests/limits
scaled accordingly, and this environment's DB host / CORS origin / ingress
host patched onto the shared base config.

Apply with (requires a real cluster and kubectl context -- this has not been
run through `kustomize build` or `kubectl apply --dry-run`, only validated as
YAML; treat any error you hit running it for real as a real bug to fix, the
same caveat the root README gives the Java services):

    kubectl apply -k infrastructure/kubernetes/overlays/dev

`DB_HOST` and the ServiceAccount role ARNs are placeholders
(`REPLACE_WITH_...`) until wired to the real outputs of
`infrastructure/terraform/environments/dev` -- see that directory's README.
The `.github/workflows/cd-dev.yml` pipeline is what actually runs this in
practice, after setting the real image tag with `kustomize edit set image`.
