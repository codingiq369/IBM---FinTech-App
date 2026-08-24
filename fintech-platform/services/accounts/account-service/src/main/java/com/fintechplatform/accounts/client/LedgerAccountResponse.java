package com.fintechplatform.accounts.client;

import java.time.Instant;
import java.util.UUID;

/** Mirrors the response shape of ledger-service's POST /api/ledger/accounts. */
public record LedgerAccountResponse(UUID id, UUID ownerReference, String currency, Instant createdAt) {}
