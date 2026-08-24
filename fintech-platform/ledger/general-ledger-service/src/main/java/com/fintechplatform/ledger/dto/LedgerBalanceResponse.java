package com.fintechplatform.ledger.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LedgerBalanceResponse(UUID ledgerAccountId, BigDecimal balance, String currency) {}
