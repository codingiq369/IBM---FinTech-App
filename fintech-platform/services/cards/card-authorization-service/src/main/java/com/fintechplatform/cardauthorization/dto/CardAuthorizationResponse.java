package com.fintechplatform.cardauthorization.dto;

import com.fintechplatform.cardauthorization.domain.CardAuthorization;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CardAuthorizationResponse(
        UUID id,
        UUID cardId,
        UUID accountId,
        String merchantName,
        BigDecimal amount,
        String currency,
        String status,
        UUID journalEntryReference,
        String declineReason,
        Instant createdAt) {

    public static CardAuthorizationResponse from(CardAuthorization authorization) {
        return new CardAuthorizationResponse(
                authorization.getId(),
                authorization.getCardId(),
                authorization.getAccountId(),
                authorization.getMerchantName(),
                authorization.getAmount(),
                authorization.getCurrency(),
                authorization.getStatus().name(),
                authorization.getJournalEntryReference(),
                authorization.getDeclineReason(),
                authorization.getCreatedAt());
    }
}
