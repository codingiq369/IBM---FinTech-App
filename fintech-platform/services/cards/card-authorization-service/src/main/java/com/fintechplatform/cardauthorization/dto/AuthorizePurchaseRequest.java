package com.fintechplatform.cardauthorization.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.UUID;

public record AuthorizePurchaseRequest(
        @NotNull(message = "cardId is required") UUID cardId,
        @NotBlank(message = "merchantName is required") String merchantName,
        @NotNull(message = "amount is required") @DecimalMin(value = "0.01", message = "amount must be positive") BigDecimal amount,
        @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO code, e.g. USD") String currency) {

    public String currencyOrDefault() {
        return currency == null || currency.isBlank() ? "USD" : currency;
    }
}
