package com.fintechplatform.transfers.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintechplatform.transfers.domain.Transfer;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

/**
 * No embedded/real Kafka broker here, on purpose — same style as the rest
 * of this codebase's unit tests (e.g. {@code TransferExecutionServiceTest}
 * mocking {@code LedgerClient} rather than standing up a real
 * ledger-service). What matters for this class is its contract: it sends
 * the right topic/key/payload, and a failure to send never becomes an
 * exception the caller has to handle.
 */
class TransferEventPublisherTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @SuppressWarnings("unchecked")
    void publishesTheTransferAsATransferCompletedEventKeyedByTransferId() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        TransferEventPublisher publisher = new TransferEventPublisher(kafkaTemplate, objectMapper);
        Transfer transfer = new Transfer(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("42.50"), "USD");
        transfer.markCompleted(UUID.randomUUID());

        CompletableFuture<SendResult<String, String>> completed = CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("transaction-events", transfer.getId().toString(), "{}"), mock(RecordMetadata.class)));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(completed);

        publisher.publishTransferCompleted(transfer);

        // The exact JSON isn't asserted here (brittle against field-order
        // changes); what matters is the topic, the key (the transfer id,
        // so all events for one transfer land on the same partition), and
        // that the serialized payload actually contains this event's type.
        verify(kafkaTemplate).send(eq("transaction-events"), eq(transfer.getId().toString()), contains("TransferCompleted"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void aSendFailureIsSwallowedNotThrown() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        TransferEventPublisher publisher = new TransferEventPublisher(kafkaTemplate, objectMapper);
        Transfer transfer = new Transfer(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("10.00"), "USD");
        transfer.markCompleted(UUID.randomUUID());

        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("broker unreachable"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(failed);

        assertThatCode(() -> publisher.publishTransferCompleted(transfer)).doesNotThrowAnyException();
    }

    @Test
    @SuppressWarnings("unchecked")
    void aSynchronousKafkaClientExceptionIsAlsoSwallowed() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        TransferEventPublisher publisher = new TransferEventPublisher(kafkaTemplate, objectMapper);
        Transfer transfer = new Transfer(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("10.00"), "USD");
        transfer.markCompleted(UUID.randomUUID());

        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("serializer misconfigured"));

        assertThatCode(() -> publisher.publishTransferCompleted(transfer)).doesNotThrowAnyException();
    }
}
