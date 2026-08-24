package com.fintechplatform.ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for ledger-service, the general ledger.
 *
 * <p>This is the one place in the whole slice where "double-entry
 * bookkeeping" is actually enforced: every {@code JournalEntry} must carry
 * postings whose debits and credits sum to exactly the same amount, or it is
 * rejected outright. No other service is allowed to move money without
 * going through here, and no other service stores a balance — balances are
 * always computed from this service's postings.
 */
@SpringBootApplication
public class LedgerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LedgerServiceApplication.class, args);
    }
}
