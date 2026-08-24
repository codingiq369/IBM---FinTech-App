package com.fintechplatform.accounts.dto;

import com.fintechplatform.accounts.domain.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record OpenAccountRequest(
        @NotNull(message = "customerId is required") UUID customerId,
        @NotNull(message = "accountType is required") AccountType accountType,
        @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO code, e.g. USD") String currency) {

    public String currencyOrDefault() {
        return currency == null || currency.isBlank() ? "USD" : currency;
    }
}
