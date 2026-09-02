package com.fintechplatform.cardauthorization.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintechplatform.cardauthorization.domain.CardAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes {@link CardAuthorizationApprovedEvent} to
 * {@code card-authorization-events} on a best-effort basis, after the
 * authorization has already been committed as APPROVED.
 *
 * <p>Mirrors {@code com.fintechplatform.transfers.event.TransferEventPublisher}
 * exactly: never part of the transaction that saved the CardAuthorization,
 * never able to throw. A shopper whose card was just approved must not see
 * that purchase fail because Kafka happened to be unreachable at that
 * moment — we log a warning and move on. Publishes to the shared
 * {@code transaction-events} topic (see ADR-0003), the same one
 * {@code TransferEventPublisher} uses — both event types are, at bottom,
 * "a ledger-backed transaction happened," distinguished by each event's
 * {@code eventType} field, rather than one topic per producing service.
 * The message key is the card authorization id, so a given authorization's
 * events (today: exactly one) stay ordered relative to each other on the
 * same partition.
 */
@Component
public class CardAuthorizationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CardAuthorizationEventPublisher.class);
    private static final String TOPIC = "transaction-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CardAuthorizationEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishCardAuthorizationApproved(CardAuthorization authorization) {
        try {
            CardAuthorizationApprovedEvent event = CardAuthorizationApprovedEvent.forAuthorization(
                    authorization.getId(),
                    authorization.getCardId(),
                    authorization.getAccountId(),
                    authorization.getMerchantName(),
                    authorization.getAmount(),
                    authorization.getCurrency(),
                    authorization.getJournalEntryReference());
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, authorization.getId().toString(), payload).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.warn("Failed to publish CardAuthorizationApproved for authorization {}: {}", authorization.getId(), ex.getMessage());
                }
            });
        } catch (Exception e) {
            // Same reasoning as TransferEventPublisher: a publishing
            // problem must never change an authorization decision that
            // has already been made and committed.
            log.warn("Failed to publish CardAuthorizationApproved for authorization {}: {}", authorization.getId(), e.getMessage());
        }
    }
}
