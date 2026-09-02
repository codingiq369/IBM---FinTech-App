package com.fintechplatform.cardauthorization.client;

import java.time.Instant;
import java.util.UUID;

/** Mirrors accounts-service's AccountResponse — just the fields
 * card-authorization-service needs. */
public record AccountResponse(
        UUID id,
        UUID customerId,
        UUID ledgerAccountId,
        String accountNumber,
        String accountType,
        String currency,
        String status,
        Instant createdAt) {}
