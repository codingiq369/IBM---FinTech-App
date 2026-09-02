package com.fintechplatform.cardauthorization.client;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Mirrors card-management-service's CardResponse — just the fields
 * card-authorization-service needs to decide on a purchase. */
public record CardResponse(
        UUID id,
        UUID accountId,
        UUID customerId,
        String cardNumberMasked,
        String cardholderName,
        String cardType,
        int expiryMonth,
        int expiryYear,
        String status,
        BigDecimal dailyPurchaseLimit,
        Instant createdAt,
        Instant activatedAt,
        Instant blockedAt) {

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
