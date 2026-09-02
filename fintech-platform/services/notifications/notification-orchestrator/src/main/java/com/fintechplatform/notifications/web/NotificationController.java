package com.fintechplatform.notifications.web;

import com.fintechplatform.notifications.dto.NotificationResponse;
import com.fintechplatform.notifications.service.NotificationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only, on purpose: this service has no {@code POST} endpoint at all.
 * The only way a {@code NotificationRecord} is created is
 * {@link com.fintechplatform.notifications.event.TransactionEventListener}
 * reacting to a Kafka message — nothing calls this service over HTTP to
 * tell it something happened.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** The most recent 50 notifications, newest first — enough for a
     * "recent activity" feed in the demo UI without needing pagination
     * for this sprint's scope. */
    @GetMapping
    public List<NotificationResponse> getRecent() {
        return notificationService.getRecent().stream().map(NotificationResponse::from).toList();
    }
}
