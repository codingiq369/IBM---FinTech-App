package com.fintechplatform.cardauthorization.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The fact that a card purchase was approved, for anything downstream that
 * wants to react (notifications today) without needing to ask
 * card-authorization-service directly. Mirrors
 * {@code com.fintechplatform.transfers.event.TransferCompletedEvent} —
 * see that class's Javadoc for the reasoning that applies equally here:
 * this is a record of something already committed, never itself the source
 * of truth, and never published for a DECLINED outcome.
 */
public record CardAuthorizationApprovedEvent(
        UUID eventId,
        String eventType,
        UUID cardAuthorizationId,
        UUID cardId,
        UUID accountId,
        String merchantName,
        BigDecimal amount,
        String currency,
        UUID journalEntryReference,
        Instant occurredAt) {

    public static final String EVENT_TYPE = "CardAuthorizationApproved";

    public static CardAuthorizationApprovedEvent forAuthorization(
            UUID cardAuthorizationId,
            UUID cardId,
            UUID accountId,
            String merchantName,
            BigDecimal amount,
            String currency,
            UUID journalEntryReference) {
        return new CardAuthorizationApprovedEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                cardAuthorizationId,
                cardId,
                accountId,
                merchantName,
                amount,
                currency,
                journalEntryReference,
                Instant.now());
    }
}
