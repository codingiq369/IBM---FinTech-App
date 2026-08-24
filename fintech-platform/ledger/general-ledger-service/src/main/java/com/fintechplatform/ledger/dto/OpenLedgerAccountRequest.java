package com.fintechplatform.ledger.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record OpenLedgerAccountRequest(
        @NotNull(message = "ownerReference is required") UUID ownerReference,
        @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO code, e.g. USD") String currency) {

    public String currencyOrDefault() {
        return currency == null || currency.isBlank() ? "USD" : currency;
    }
}
