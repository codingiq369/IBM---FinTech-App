package com.fintechplatform.notifications.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

/**
 * One transaction-events message this service has already acted on — an
 * append-only audit trail, never updated after it's written. There is no
 * status field and no lifecycle: unlike a Transfer or a CardAuthorization,
 * a notification record doesn't move through states, it just accumulates.
 *
 * <p>{@code eventId} carries a unique constraint (see V1 migration) so
 * that redelivery of the same Kafka message — which at-least-once delivery
 * guarantees will eventually do, e.g. after a consumer-group rebalance —
 * produces one row, not one row per delivery. This is a narrower guarantee
 * than a full idempotent-consumption design: it stops duplicate rows, not
 * duplicate side effects, which is all this service needs since writing
 * this row *is* its only side effect. See ADR-0003 for why a heavier
 * dedup mechanism isn't built here yet.
 */
@Entity
@Table(name = "notification_records", uniqueConstraints = @UniqueConstraint(columnNames = "event_id"))
public class NotificationRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    /** The transfer id or card authorization id the source event was
     * about — kept generic rather than two nullable columns, since this
     * service treats every event type the same way: something happened,
     * here's what to show about it. */
    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(nullable = false, length = 500)
    private String summary;

    /** The raw JSON payload, kept for audit/debugging — if a summary ever
     * looks wrong, this is how you find out what was actually published
     * without needing to replay Kafka. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    protected NotificationRecord() {
        // JPA
    }

    public NotificationRecord(UUID eventId, String eventType, UUID referenceId, String summary, String payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.referenceId = referenceId;
        this.summary = summary;
        this.payload = payload;
        this.receivedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public String getSummary() {
        return summary;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}
