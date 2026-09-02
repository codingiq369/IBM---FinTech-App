package com.fintechplatform.cardauthorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for card-authorization-service.
 *
 * <p>This service answers "is this purchase approved, right now, and why" —
 * the card-present, real-time decision a payment network makes in
 * milliseconds. It consolidates what the fuller scaffold names as two
 * separate services (card-authorization-service and card-transaction-service):
 * here, approving a purchase and posting its ledger movement are the same
 * step, the same way internal-transfer-service both decides and executes a
 * transfer. See ADR-0010 for why a purchase settles against a card-network
 * clearing ledger account instead of a real merchant-acquiring domain.
 */
@SpringBootApplication
public class CardAuthorizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardAuthorizationServiceApplication.class, args);
    }
}
