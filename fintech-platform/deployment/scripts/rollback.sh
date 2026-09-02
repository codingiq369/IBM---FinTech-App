#!/usr/bin/env bash
# Manual equivalent of .github/workflows/rollback.yml -- kubectl rollout
# undo for one Deployment in one environment. See
# ci-cd/release-management/rollback-policy.md for when to use this vs.
# rolling forward with a fix.
#
# Usage: rollback.sh <staging|uat|production> <deployment-name>
#   deployment-name is one of: customer-service, accounts-service,
#   ledger-service, transfers-service, api-gateway, web-banking
#
# Requires a kubeconfig already pointed at that environment's cluster.
set -euo pipefail

ENV="${1:?usage: rollback.sh <staging|uat|production> <deployment-name>}"
DEPLOYMENT="${2:?usage: rollback.sh <staging|uat|production> <deployment-name>}"

case "$ENV" in
  staging|uat|production) ;;
  *) echo "unknown environment: $ENV (expected staging, uat, or production -- development just re-runs 'docker compose up --build')" >&2; exit 1 ;;
esac

command -v kubectl >/dev/null 2>&1 || { echo "kubectl is required" >&2; exit 1; }

NAMESPACE="fintech-$ENV"
echo "==> Rolling back deployment/$DEPLOYMENT in $NAMESPACE"
kubectl rollout undo "deployment/$DEPLOYMENT" -n "$NAMESPACE"
kubectl rollout status "deployment/$DEPLOYMENT" -n "$NAMESPACE" --timeout=180s
echo "==> Done. Verify with: deployment/scripts/health-check.sh $NAMESPACE --k8s"
