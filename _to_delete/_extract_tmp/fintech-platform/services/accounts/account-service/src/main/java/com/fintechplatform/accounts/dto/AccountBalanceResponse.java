package com.fintechplatform.accounts.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** What we hand back for GET /api/accounts/{id}/balance, after asking ledger-service to compute it. */
public record AccountBalanceResponse(UUID accountId, String accountNumber, BigDecimal balance, String currency) {}
