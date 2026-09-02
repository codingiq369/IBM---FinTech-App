#!/usr/bin/env bash
# Deploys the vertical slice to one environment.
#
#   deploy.sh development [image_tag]   # local docker-compose (no cluster needed)
#   deploy.sh staging|uat|production <image_tag>   # kubectl apply -k against
#     that environment's Kustomize overlay -- requires a kubeconfig already
#     pointed at that environment's cluster (see
#     infrastructure/kubernetes/overlays/<env>/README.md) and AWS
#     credentials for `kustomize edit set image` to have pulled from.
#
# This is the manual/local equivalent of what
# .github/workflows/deploy-<env>.yml runs in CI -- reach for that instead
# for anything that should be repeatable and audited; this script is for a
# human running a deploy by hand (e.g. against a fresh dev cluster, or to
# reproduce a CI deploy step locally while debugging it).
set -euo pipefail

ENV="${1:?usage: deploy.sh <development|staging|uat|production> [image_tag]}"

if [ "$ENV" = "development" ] || [ "$ENV" = "dev" ]; then
  echo "==> Starting the dev stack with docker compose"
  (cd deployment/docker && docker compose --env-file ../../environments/development/.env -f docker-compose.yml up --build -d)
  echo "==> Waiting for services to come up"
  sleep 5
  bash "$(dirname "$0")/health-check.sh" development
  exit 0
fi

case "$ENV" in
  staging|uat|production) ;;
  *) echo "unknown environment: $ENV (expected development, staging, uat, or production)" >&2; exit 1 ;;
esac

IMAGE_TAG="${2:?usage: deploy.sh $ENV <image_tag>}"
command -v kustomize >/dev/null 2>&1 || { echo "kustomize is required" >&2; exit 1; }
command -v kubectl >/dev/null 2>&1 || { echo "kubectl is required" >&2; exit 1; }

REPO_REF="${IMAGE_REPO:?set IMAGE_REPO, e.g. ghcr.io/<org>/fintech-platform}"
OVERLAY="infrastructure/kubernetes/overlays/$ENV"

echo "==> Pointing $OVERLAY at $REPO_REF/*:$IMAGE_TAG"
(cd "$OVERLAY" && for svc in customer-service accounts-service ledger-service transfers-service api-gateway web-banking; do
  kustomize edit set image "$REPO_REF/$svc=$REPO_REF/$svc:$IMAGE_TAG"
done)

echo "==> Applying"
kubectl apply -k "$OVERLAY"

echo "==> Waiting for rollout"
for svc in customer-service accounts-service ledger-service transfers-service api-gateway web-banking; do
  kubectl rollout status "deployment/$svc" -n "fintech-$ENV" --timeout=180s
done

bash "$(dirname "$0")/health-check.sh" "fintech-$ENV" --k8s
