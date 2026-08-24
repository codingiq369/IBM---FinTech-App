package com.fintechplatform.transfers.client;

import java.time.Instant;
import java.util.UUID;

/** Mirrors accounts-service's AccountResponse — just the fields transfers-service needs. */
public record AccountResponse(
        UUID id,
        UUID customerId,
        UUID ledgerAccountId,
        String accountNumber,
        String accountType,
        String currency,
        String status,
        Instant createdAt) {

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
