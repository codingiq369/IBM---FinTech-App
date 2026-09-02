package com.fintechplatform.notifications.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransactionEventParserTest {

    private final TransactionEventParser parser = new TransactionEventParser(new ObjectMapper().findAndRegisterModules());

    @Test
    void parsesATransferCompletedPayload() {
        UUID eventId = UUID.randomUUID();
        UUID transferId = UUID.randomUUID();
        String payload = """
                {
                  "eventId": "%s",
                  "eventType": "TransferCompleted",
                  "transferId": "%s",
                  "sourceAccountId": "%s",
                  "destinationAccountId": "%s",
                  "amount": 25.00,
                  "currency": "USD",
                  "journalEntryReference": "%s",
                  "occurredAt": "2026-09-02T12:00:00Z"
                }
                """.formatted(eventId, transferId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        ParsedTransactionEvent parsed = parser.parse(payload);

        assertThat(parsed.eventId()).isEqualTo(eventId);
        assertThat(parsed.eventType()).isEqualTo("TransferCompleted");
        assertThat(parsed.referenceId()).isEqualTo(transferId);
        assertThat(parsed.summary()).contains("25.0", "USD", transferId.toString());
    }

    @Test
    void parsesACardAuthorizationApprovedPayload() {
        UUID eventId = UUID.randomUUID();
        UUID cardAuthorizationId = UUID.randomUUID();
        String payload = """
                {
                  "eventId": "%s",
                  "eventType": "CardAuthorizationApproved",
                  "cardAuthorizationId": "%s",
                  "cardId": "%s",
                  "accountId": "%s",
                  "merchantName": "Coffee Shop",
                  "amount": 4.50,
                  "currency": "USD",
                  "journalEntryReference": "%s",
                  "occurredAt": "2026-09-02T12:00:00Z"
                }
                """.formatted(eventId, cardAuthorizationId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        ParsedTransactionEvent parsed = parser.parse(payload);

        assertThat(parsed.eventId()).isEqualTo(eventId);
        assertThat(parsed.eventType()).isEqualTo("CardAuthorizationApproved");
        assertThat(parsed.referenceId()).isEqualTo(cardAuthorizationId);
        assertThat(parsed.summary()).contains("4.5", "USD", "Coffee Shop", cardAuthorizationId.toString());
    }

    @Test
    void malformedJsonIsRejected() {
        assertThatThrownBy(() -> parser.parse("not json")).isInstanceOf(UnrecognizedEventException.class);
    }

    @Test
    void anUnrecognizedEventTypeIsRejected() {
        String payload = """
                {"eventId": "%s", "eventType": "SomethingElseEntirely"}
                """.formatted(UUID.randomUUID());

        assertThatThrownBy(() -> parser.parse(payload))
                .isInstanceOf(UnrecognizedEventException.class)
                .hasMessageContaining("SomethingElseEntirely");
    }

    @Test
    void aMissingRequiredFieldIsRejected() {
        String payload = """
                {"eventId": "%s", "eventType": "TransferCompleted"}
                """.formatted(UUID.randomUUID());

        assertThatThrownBy(() -> parser.parse(payload)).isInstanceOf(UnrecognizedEventException.class);
    }
}
