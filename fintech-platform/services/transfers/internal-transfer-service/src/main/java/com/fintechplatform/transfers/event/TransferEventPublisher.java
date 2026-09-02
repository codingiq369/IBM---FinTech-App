package com.fintechplatform.transfers.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintechplatform.transfers.domain.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes {@link TransferCompletedEvent} to {@code transfer-events} on a
 * best-effort basis, after the transfer has already been committed as
 * COMPLETED.
 *
 * <p>Deliberately not part of the transaction that saves the Transfer, and
 * deliberately unable to throw: this is a notification about something that
 * already happened, not a step in making it happen. If Kafka is unreachable,
 * slow, or the topic doesn't exist yet, the transfer itself must still be a
 * success from the caller's point of view — we log a warning and move on,
 * the same way {@code TransferExecutionService} never lets a downstream
 * problem corrupt state it has already decided is correct. A production
 * system that needed stronger delivery guarantees than "best effort" would
 * reach for a transactional outbox here; seeing that need in practice is
 * exactly why this slice starts with the simpler thing first (see
 * ADR-0003).
 *
 * <p>Publishes to the shared {@code transaction-events} topic (see
 * ADR-0003) rather than a topic of its own — every event this platform
 * emits today ultimately traces back to something posted through
 * ledger-service, so "a ledger-backed transaction happened" is the one
 * topic both this class and {@code CardAuthorizationEventPublisher}
 * publish onto, distinguished by each event's {@code eventType} field. The
 * Kafka message key is the transfer id, so all events for a given transfer
 * (today: exactly one) land on the same partition and preserve order
 * relative to each other.
 */
@Component
public class TransferEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransferEventPublisher.class);
    private static final String TOPIC = "transaction-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public TransferEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishTransferCompleted(Transfer transfer) {
        try {
            TransferCompletedEvent event = TransferCompletedEvent.forTransfer(
                    transfer.getId(),
                    transfer.getSourceAccountId(),
                    transfer.getDestinationAccountId(),
                    transfer.getAmount(),
                    transfer.getCurrency(),
                    transfer.getJournalEntryReference());
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, transfer.getId().toString(), payload).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.warn("Failed to publish TransferCompleted for transfer {}: {}", transfer.getId(), ex.getMessage());
                }
            });
        } catch (Exception e) {
            // Never let a publishing problem (serialization, a Kafka client
            // error thrown synchronously rather than via the future, etc.)
            // propagate into the caller — the transfer already completed
            // successfully and that outcome must not change because of this.
            log.warn("Failed to publish TransferCompleted for transfer {}: {}", transfer.getId(), e.getMessage());
        }
    }
}
