package com.fintechplatform.notifications.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Turns a raw {@code transaction-events} JSON string into a
 * {@link ParsedTransactionEvent}, dispatching on the {@code eventType}
 * field to build a human-readable summary from whichever fields that
 * event type actually carries. Adding a third event type to the topic
 * means adding a branch here — deliberately a simple if/else rather than
 * a registered-strategy abstraction, since two branches don't earn one
 * yet.
 */
@Component
public class TransactionEventParser {

    private final ObjectMapper objectMapper;

    public TransactionEventParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ParsedTransactionEvent parse(String payload) {
        JsonNode node;
        try {
            node = objectMapper.readTree(payload);
        } catch (Exception e) {
            throw new UnrecognizedEventException("Payload was not valid JSON: " + e.getMessage(), e);
        }

        String eventType = textOrThrow(node, "eventType");
        UUID eventId = uuidOrThrow(node, "eventId");

        return switch (eventType) {
            case "TransferCompleted" -> parseTransferCompleted(node, eventId, eventType);
            case "CardAuthorizationApproved" -> parseCardAuthorizationApproved(node, eventId, eventType);
            default -> throw new UnrecognizedEventException("Unrecognized eventType: " + eventType);
        };
    }

    private ParsedTransactionEvent parseTransferCompleted(JsonNode node, UUID eventId, String eventType) {
        UUID transferId = uuidOrThrow(node, "transferId");
        String amount = textOrThrow(node, "amount");
        String currency = textOrThrow(node, "currency");
        String summary = "Transfer of " + amount + " " + currency + " completed (transfer " + transferId + ")";
        return new ParsedTransactionEvent(eventId, eventType, transferId, summary);
    }

    private ParsedTransactionEvent parseCardAuthorizationApproved(JsonNode node, UUID eventId, String eventType) {
        UUID cardAuthorizationId = uuidOrThrow(node, "cardAuthorizationId");
        String amount = textOrThrow(node, "amount");
        String currency = textOrThrow(node, "currency");
        String merchantName = textOrThrow(node, "merchantName");
        String summary = "Card purchase of " + amount + " " + currency + " at " + merchantName + " approved (authorization " + cardAuthorizationId + ")";
        return new ParsedTransactionEvent(eventId, eventType, cardAuthorizationId, summary);
    }

    private String textOrThrow(JsonNode node, String field) {
        if (!node.hasNonNull(field)) {
            throw new UnrecognizedEventException("Missing required field: " + field);
        }
        return node.get(field).asText();
    }

    private UUID uuidOrThrow(JsonNode node, String field) {
        try {
            return UUID.fromString(textOrThrow(node, field));
        } catch (IllegalArgumentException e) {
            throw new UnrecognizedEventException("Field " + field + " was not a valid UUID: " + e.getMessage(), e);
        }
    }
}
