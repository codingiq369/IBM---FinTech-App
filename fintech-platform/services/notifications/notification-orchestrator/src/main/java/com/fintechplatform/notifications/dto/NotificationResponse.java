package com.fintechplatform.notifications.dto;

import com.fintechplatform.notifications.domain.NotificationRecord;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(UUID id, String eventType, UUID referenceId, String summary, Instant receivedAt) {

    public static NotificationResponse from(NotificationRecord record) {
        return new NotificationResponse(
                record.getId(), record.getEventType(), record.getReferenceId(), record.getSummary(), record.getReceivedAt());
    }
}
