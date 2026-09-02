package com.fintechplatform.cardmanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record IssueCardRequest(
        @NotNull(message = "accountId is required") UUID accountId,
        @NotBlank(message = "cardholderName is required") String cardholderName,
        @DecimalMin(value = "0.01", message = "dailyPurchaseLimit must be positive") BigDecimal dailyPurchaseLimit) {

    private static final BigDecimal DEFAULT_DAILY_PURCHASE_LIMIT = new BigDecimal("2000.00");

    public BigDecimal dailyPurchaseLimitOrDefault() {
        return dailyPurchaseLimit == null ? DEFAULT_DAILY_PURCHASE_LIMIT : dailyPurchaseLimit;
    }
}
