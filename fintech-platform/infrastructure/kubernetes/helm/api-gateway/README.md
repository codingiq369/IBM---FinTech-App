# api-gateway

Placeholder scaffolding for a Helm chart, mismatched with what's actually
built: gateways/api-gateway is a real, running Spring Boot service (see the root README),
deployed today via Kustomize (`infrastructure/kubernetes/base` +
`infrastructure/kubernetes/overlays/<env>`), not Helm. This folder's
`Dockerfile`/`package.json`/`src/main.ts` describe a Node.js service that
doesn't exist -- out of scope for the multi-environment setup in this pass.
If a Helm chart is wanted for gateways/api-gateway later, it should package the same
image the Kustomize deployment already builds, not a separate service.
