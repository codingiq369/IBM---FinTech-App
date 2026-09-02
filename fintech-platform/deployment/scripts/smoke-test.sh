#!/usr/bin/env bash
# Exercises the actual implemented flow -- onboard -> open two accounts ->
# attempt a transfer -- against a running gateway. Requires curl and jq.
#
# Usage: smoke-test.sh [gateway_base_url]
#   defaults to http://localhost:8080 (the docker-compose / dev default)
#
# There is no deposit/funding endpoint anywhere in this API (see
# ledger/general-ledger-service's LedgerAccountController and
# docs/architecture/vertical-slice.md) -- a freshly opened account has a
# zero balance. So the transfer step below is expected to come back
# status=FAILED with an insufficient-funds reason; that's the ledger
# correctly enforcing its invariant, not a smoke-test failure. What this
# script actually verifies is that every step in the chain responds
# correctly end to end: onboarding approves a real adult, both accounts
# open against real ledger accounts, and the transfer request itself is
# accepted and processed (201, with a well-formed status) rather than
# erroring out.
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
UNIQUE="$(date +%s)-$$"

echo "==> Smoke testing $BASE_URL"

require() {
  command -v "$1" >/dev/null 2>&1 || { echo "missing required tool: $1" >&2; exit 1; }
}
require curl
require jq

post() {
  local path="$1" body="$2"
  curl -sS -w '\n%{http_code}' -H 'Content-Type: application/json' -X POST -d "$body" "$BASE_URL$path"
}

get() {
  local path="$1"
  curl -sS -w '\n%{http_code}' "$BASE_URL$path"
}

split_status() {
  # Splits the curl -w output above into body + status code, via stdin.
  local response="$1"
  BODY="$(echo "$response" | sed '$d')"
  STATUS="$(echo "$response" | tail -n1)"
}

expect() {
  local label="$1" expected="$2" actual="$3"
  if [ "$actual" != "$expected" ]; then
    echo "FAILED: $label -- expected HTTP $expected, got $actual" >&2
    echo "$BODY" >&2
    exit 1
  fi
  echo "ok: $label ($actual)"
}

echo "--- onboarding a customer (adult, should be KYC-approved) ---"
split_status "$(post /api/customers "$(jq -n --arg email "smoke-$UNIQUE@example.com" '{
  fullName: "Smoke Test",
  email: $email,
  dateOfBirth: "1990-01-01"
}')")"
expect "onboard customer" 201 "$STATUS"
CUSTOMER_ID="$(echo "$BODY" | jq -r .id)"
KYC_STATUS="$(echo "$BODY" | jq -r .kycStatus)"
[ "$KYC_STATUS" = "APPROVED" ] || { echo "FAILED: expected an approved customer, got $KYC_STATUS" >&2; exit 1; }
echo "customer $CUSTOMER_ID approved"

echo "--- opening the source account ---"
split_status "$(post /api/accounts "$(jq -n --arg cid "$CUSTOMER_ID" '{customerId: $cid, accountType: "CHECKING", currency: "USD"}')")"
expect "open source account" 201 "$STATUS"
SOURCE_ACCOUNT_ID="$(echo "$BODY" | jq -r .id)"

echo "--- opening the destination account ---"
split_status "$(post /api/accounts "$(jq -n --arg cid "$CUSTOMER_ID" '{customerId: $cid, accountType: "SAVINGS", currency: "USD"}')")"
expect "open destination account" 201 "$STATUS"
DEST_ACCOUNT_ID="$(echo "$BODY" | jq -r .id)"

echo "--- checking the source account's starting balance ---"
split_status "$(get "/api/accounts/$SOURCE_ACCOUNT_ID/balance")"
expect "get balance" 200 "$STATUS"
echo "starting balance: $(echo "$BODY" | jq -c .)"

echo "--- attempting a transfer (expected to fail on insufficient funds -- see header comment) ---"
split_status "$(post /api/transfers "$(jq -n --arg src "$SOURCE_ACCOUNT_ID" --arg dst "$DEST_ACCOUNT_ID" '{
  sourceAccountId: $src,
  destinationAccountId: $dst,
  amount: 10.00,
  description: "smoke test transfer"
}')")"
expect "initiate transfer" 201 "$STATUS"
TRANSFER_STATUS="$(echo "$BODY" | jq -r .status)"
echo "transfer status: $TRANSFER_STATUS ($(echo "$BODY" | jq -r '.failureReason // "no failure reason"'))"

echo "==> All steps completed. The vertical slice is responding end to end."
