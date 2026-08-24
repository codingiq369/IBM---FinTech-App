package com.fintechplatform.ledger.dto;

import com.fintechplatform.ledger.domain.Direction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PostingRequest(
        @NotNull(message = "ledgerAccountId is required") UUID ledgerAccountId,
        @NotNull(message = "direction is required (DEBIT or CREDIT)") Direction direction,
        @NotNull(message = "amount is required") @DecimalMin(value = "0.01", message = "amount must be positive") BigDecimal amount) {}
