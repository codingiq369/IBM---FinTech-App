#!/usr/bin/env bash
# Checks that every real component is up and healthy.
#
# Usage:
#   health-check.sh                 # local docker-compose (dev defaults)
#   health-check.sh dev|staging|uat|production   # a docker-compose run using
#                                                   that environment's .env
#   health-check.sh <k8s-namespace> --k8s         # a real cluster namespace,
#                                                   e.g.: health-check.sh fintech-staging --k8s
set -euo pipefail

SERVICES="customer-service:8081 accounts-service:8082 ledger-service:8083 transfers-service:8084 api-gateway:8080"

if [ "${2:-}" = "--k8s" ]; then
  NAMESPACE="$1"
  echo "==> Checking pods in namespace $NAMESPACE"
  kubectl get pods -n "$NAMESPACE" -o wide
  echo "==> Checking each Deployment's rollout status"
  for svc in customer-service accounts-service ledger-service transfers-service api-gateway web-banking; do
    kubectl rollout status "deployment/$svc" -n "$NAMESPACE" --timeout=60s
  done
  exit 0
fi

ENV="${1:-development}"
ENV_FILE="environments/$ENV/.env"
if [ ! -f "$ENV_FILE" ]; then
  echo "no such environment .env: $ENV_FILE (expected development, staging, uat, or production)" >&2
  exit 1
fi

# Pull the host ports this environment's docker-compose stack published, so
# this script works for every environment's offset ports, not just dev's.
port_for() {
  local var="$1" default="$2" value
  value="$(grep -E "^${var}=" "$ENV_FILE" | tail -n1 | cut -d= -f2)"
  echo "${value:-$default}"
}
CUSTOMER_PORT="$(port_for CUSTOMER_SERVICE_PORT 8081)"
ACCOUNTS_PORT="$(port_for ACCOUNTS_SERVICE_PORT 8082)"
LEDGER_PORT="$(port_for LEDGER_SERVICE_PORT 8083)"
TRANSFERS_PORT="$(port_for TRANSFERS_SERVICE_PORT 8084)"
GATEWAY_PORT="$(port_for API_GATEWAY_PORT 8080)"

FAILED=0
check() {
  local name="$1" port="$2"
  if curl -sf "http://localhost:${port}/actuator/health" >/dev/null; then
    echo "ok: $name (port $port)"
  else
    echo "DOWN: $name (port $port)" >&2
    FAILED=1
  fi
}

echo "==> Checking $ENV stack on localhost"
check customer-service "$CUSTOMER_PORT"
check accounts-service "$ACCOUNTS_PORT"
check ledger-service "$LEDGER_PORT"
check transfers-service "$TRANSFERS_PORT"
check api-gateway "$GATEWAY_PORT"

exit $FAILED
