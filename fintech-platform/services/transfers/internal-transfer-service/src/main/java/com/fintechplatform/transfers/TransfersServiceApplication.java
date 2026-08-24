package com.fintechplatform.transfers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for transfers-service.
 *
 * <p>This service is the customer-facing idea of "move money from A to B".
 * It never touches a ledger table directly — it calls accounts-service to
 * resolve and validate both ends, then calls ledger-service to post the
 * actual balanced journal entry. If the ledger call fails, the Transfer
 * itself is still saved with status FAILED, because "we tried to move money
 * and it didn't work" is exactly the kind of thing a bank needs an audit
 * trail for, not something to just return an error and forget.
 */
@SpringBootApplication
public class TransfersServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransfersServiceApplication.class, args);
    }
}
