package com.fintechplatform.cardmanagement.dto;

import com.fintechplatform.cardmanagement.domain.Card;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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

    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getAccountId(),
                card.getCustomerId(),
                card.getCardNumberMasked(),
                card.getCardholderName(),
                card.getCardType().name(),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                card.getStatus().name(),
                card.getDailyPurchaseLimit(),
                card.getCreatedAt(),
                card.getActivatedAt(),
                card.getBlockedAt());
    }
}
