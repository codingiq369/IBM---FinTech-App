package com.fintechplatform.transfers.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The fact that a transfer completed, shaped for anything downstream that
 * wants to react — notifications today, fraud/reporting/analytics later —
 * without any of them needing to ask internal-transfer-service directly.
 *
 * <p>This is a notification of something that already happened and was
 * already committed to {@code transfers_db}; it is never itself the source
 * of truth for whether the transfer completed. A consumer that never
 * receives this event (because Kafka was down, say) does not change what
 * actually happened — it just doesn't get to react to it. See
 * {@link TransferEventPublisher} for why publishing this can never fail the
 * transfer itself, and ADR-0003 for why the payload is plain JSON rather
 * than a schema-registry-backed format.
 */
public record TransferCompletedEvent(
        UUID eventId,
        String eventType,
        UUID transferId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        UUID journalEntryReference,
        Instant occurredAt) {

    public static final String EVENT_TYPE = "TransferCompleted";

    public static TransferCompletedEvent forTransfer(
            UUID transferId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency,
            UUID journalEntryReference) {
        return new TransferCompletedEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                transferId,
                sourceAccountId,
                destinationAccountId,
                amount,
                currency,
                journalEntryReference,
                Instant.now());
    }
}
