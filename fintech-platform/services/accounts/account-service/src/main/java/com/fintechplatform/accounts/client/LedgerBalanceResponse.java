package com.fintechplatform.accounts.client;

import java.math.BigDecimal;
import java.util.UUID;

/** Mirrors the response shape of ledger-service's GET /api/ledger/accounts/{id}/balance. */
public record LedgerBalanceResponse(UUID ledgerAccountId, BigDecimal balance, String currency) {}
