package com.fintechplatform.cardmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for card-management-service.
 *
 * <p>This service answers "which cards exist, who they belong to, and what
 * state are they in" — it does NOT decide whether a purchase is approved
 * (that's card-authorization-service) and it does NOT store balances (that's
 * ledger-service, reached indirectly through accounts-service). Keeping that
 * boundary sharp is the same discipline accounts-service applies: this
 * service calls out to accounts-service over HTTP to confirm an account
 * exists and is active before issuing a card against it, rather than reading
 * its database.
 */
@SpringBootApplication
public class CardManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardManagementServiceApplication.class, args);
    }
}
