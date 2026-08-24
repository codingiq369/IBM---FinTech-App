package com.fintechplatform.ledger.domain;

/** Thrown when someone tries to construct a {@link JournalEntry} whose
 * debits and credits don't sum to the same total. This is a domain
 * invariant violation, not a request-validation nicety — it can never be
 * bypassed by calling code. */
public class UnbalancedJournalEntryException extends RuntimeException {
    public UnbalancedJournalEntryException(String message) {
        super(message);
    }
}
