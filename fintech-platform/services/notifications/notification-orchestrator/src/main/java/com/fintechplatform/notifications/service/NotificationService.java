package com.fintechplatform.notifications.service;

import com.fintechplatform.notifications.domain.NotificationRecord;
import com.fintechplatform.notifications.event.ParsedTransactionEvent;
import com.fintechplatform.notifications.repository.NotificationRecordRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRecordRepository repository;

    public NotificationService(NotificationRecordRepository repository) {
        this.repository = repository;
    }

    /**
     * Records one already-parsed transaction event, unless a row for its
     * {@code eventId} already exists — Kafka's at-least-once delivery
     * means the same message can arrive more than once (e.g. after a
     * consumer-group rebalance), and this check is what keeps that from
     * becoming duplicate rows in the feed. The existence check and the
     * save are not wrapped in an explicit transaction here: each is
     * already its own atomic repository call, and the unique constraint
     * on {@code event_id} (see V1 migration) is the actual backstop
     * against a race between two overlapping deliveries — the same
     * belt-and-suspenders reasoning documented on {@code ClearingAccountService}
     * in card-authorization-service.
     *
     * @param rawPayload the original JSON string, kept on the saved row
     *                    for audit/debugging (see {@code NotificationRecord}).
     */
    public void recordEvent(ParsedTransactionEvent event, String rawPayload) {
        if (repository.existsByEventId(event.eventId())) {
            log.info("Ignoring already-recorded event {} ({})", event.eventId(), event.eventType());
            return;
        }
        repository.save(new NotificationRecord(event.eventId(), event.eventType(), event.referenceId(), event.summary(), rawPayload));
        log.info("Recorded {}: {}", event.eventType(), event.summary());
    }

    public List<NotificationRecord> getRecent() {
        return repository.findTop50ByOrderByReceivedAtDesc();
    }
}
