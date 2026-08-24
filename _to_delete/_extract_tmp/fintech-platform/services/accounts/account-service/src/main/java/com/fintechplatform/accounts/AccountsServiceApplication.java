package com.fintechplatform.accounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for accounts-service.
 *
 * <p>This service answers "which accounts exist and who owns them". It does
 * NOT store balances or transaction history — that belongs to ledger-service.
 * Keeping that boundary sharp is the point of this slice: accounts-service
 * calls out to ledger-service over HTTP rather than reading its database,
 * which is what lets each service be deployed, scaled, and reasoned about
 * independently.
 */
@SpringBootApplication
public class AccountsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountsServiceApplication.class, args);
    }
}
