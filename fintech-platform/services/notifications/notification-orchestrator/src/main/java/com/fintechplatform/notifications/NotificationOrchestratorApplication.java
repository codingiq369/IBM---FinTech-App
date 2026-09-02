package com.fintechplatform.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for notification-orchestrator.
 *
 * <p>This service has no domain of its own to protect the way
 * ledger-service protects "debits equal credits" — its only job is to
 * listen to {@code transaction-events} (see ADR-0003) and turn each event
 * into a row a "recent activity" feed can read back. It never calls any
 * other service, and the only way anything is written to its database is
 * a Kafka message arriving — there is deliberately no
 * {@code POST /api/notifications}.
 */
@SpringBootApplication
public class NotificationOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationOrchestratorApplication.class, args);
    }
}
