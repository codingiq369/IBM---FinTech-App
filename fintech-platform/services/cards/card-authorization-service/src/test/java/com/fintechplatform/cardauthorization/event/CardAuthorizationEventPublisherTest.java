package com.fintechplatform.cardauthorization.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintechplatform.cardauthorization.domain.CardAuthorization;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

/**
 * Mirrors {@code com.fintechplatform.transfers.event.TransferEventPublisherTest}:
 * no embedded/real Kafka broker, just a mocked {@link KafkaTemplate}
 * asserting the topic/key/payload contract and that a failed send never
 * becomes an exception the caller has to handle.
 */
class CardAuthorizationEventPublisherTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @SuppressWarnings("unchecked")
    void publishesTheAuthorizationAsACardAuthorizationApprovedEventKeyedByAuthorizationId() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        CardAuthorizationEventPublisher publisher = new CardAuthorizationEventPublisher(kafkaTemplate, objectMapper);
        CardAuthorization authorization = approvedAuthorization();

        CompletableFuture<SendResult<String, String>> completed = CompletableFuture.completedFuture(new SendResult<>(
                new ProducerRecord<>("transaction-events", authorization.getId().toString(), "{}"), mock(RecordMetadata.class)));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(completed);

        publisher.publishCardAuthorizationApproved(authorization);

        verify(kafkaTemplate).send(
                eq("transaction-events"), eq(authorization.getId().toString()), contains("CardAuthorizationApproved"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void aSendFailureIsSwallowedNotThrown() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        CardAuthorizationEventPublisher publisher = new CardAuthorizationEventPublisher(kafkaTemplate, objectMapper);
        CardAuthorization authorization = approvedAuthorization();

        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("broker unreachable"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(failed);

        assertThatCode(() -> publisher.publishCardAuthorizationApproved(authorization)).doesNotThrowAnyException();
    }

    @Test
    @SuppressWarnings("unchecked")
    void aSynchronousKafkaClientExceptionIsAlsoSwallowed() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        CardAuthorizationEventPublisher publisher = new CardAuthorizationEventPublisher(kafkaTemplate, objectMapper);
        CardAuthorization authorization = approvedAuthorization();

        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("serializer misconfigured"));

        assertThatCode(() -> publisher.publishCardAuthorizationApproved(authorization)).doesNotThrowAnyException();
    }

    private CardAuthorization approvedAuthorization() {
        CardAuthorization authorization = CardAuthorization.pendingLedgerDecision(
                UUID.randomUUID(), UUID.randomUUID(), "Coffee Shop", new BigDecimal("4.50"), "USD");
        authorization.markApproved(UUID.randomUUID());
        return authorization;
    }
}
