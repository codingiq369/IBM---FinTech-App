package com.fintechplatform.transfers.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record InitiateTransferRequest(
        @NotNull(message = "sourceAccountId is required") UUID sourceAccountId,
        @NotNull(message = "destinationAccountId is required") UUID destinationAccountId,
        @NotNull(message = "amount is required") @DecimalMin(value = "0.01", message = "amount must be positive") BigDecimal amount,
        String description) {

    public String descriptionOrDefault() {
        return description == null || description.isBlank() ? "Transfer between accounts" : description;
    }
}
