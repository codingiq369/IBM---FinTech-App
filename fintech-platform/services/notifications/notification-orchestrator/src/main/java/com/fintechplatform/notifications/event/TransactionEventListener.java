package com.fintechplatform.notifications.event;

import com.fintechplatform.notifications.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * The entire point of this service: subscribe to {@code transaction-events}
 * (see ADR-0003) and turn each message into a {@link com.fintechplatform.notifications.domain.NotificationRecord}.
 *
 * <p>A parse failure (malformed JSON, an unrecognized {@code eventType},
 * a field this consumer expected but didn't find) is logged and the
 * message is otherwise dropped — it does not retry forever and does not
 * crash the consumer thread. With Spring Kafka's default acknowledgment
 * mode, returning normally from this method (including after catching an
 * exception, as done here) commits the offset, so a message this consumer
 * can't make sense of is not retried infinitely; it's simply not one that
 * shows up in the notification feed. A dead-letter topic for messages
 * that fail to parse would be the natural next step if this ever needs to
 * become more than the "smallest possible taste" this sprint set out to
 * build (see ADR-0003).
 */
@Component
public class TransactionEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventListener.class);

    private final TransactionEventParser parser;
    private final NotificationService notificationService;

    public TransactionEventListener(TransactionEventParser parser, NotificationService notificationService) {
        this.parser = parser;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "transaction-events", groupId = "notification-orchestrator")
    public void onMessage(String payload) {
        try {
            ParsedTransactionEvent event = parser.parse(payload);
            notificationService.recordEvent(event, payload);
        } catch (UnrecognizedEventException e) {
            log.warn("Could not process a transaction-events message, skipping it: {}", e.getMessage());
        } catch (Exception e) {
            // Anything else (e.g. a database problem) is also logged and
            // swallowed rather than thrown back into the Kafka container:
            // this consumer must never crash the whole application over
            // one bad or unluckily-timed message.
            log.error("Unexpected error processing a transaction-events message, skipping it", e);
        }
    }
}
