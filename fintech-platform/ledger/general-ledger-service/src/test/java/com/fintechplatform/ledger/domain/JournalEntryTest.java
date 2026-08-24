package com.fintechplatform.ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * This is the single most important test in the whole slice: it proves the
 * ledger cannot represent an unbalanced movement of money, no matter what a
 * caller sends it. If this test passes, "debits always equal credits" is
 * not a policy someone has to remember to enforce — it's structurally true.
 */
class JournalEntryTest {

    @Test
    void balancedPostingsConstructSuccessfully() {
        UUID accountA = UUID.randomUUID();
        UUID accountB = UUID.randomUUID();

        JournalEntry entry = new JournalEntry(
                "Transfer $50",
                "transfer-123",
                List.of(
                        new Posting(accountA, Direction.DEBIT, new BigDecimal("50.00")),
                        new Posting(accountB, Direction.CREDIT, new BigDecimal("50.00"))));

        assertThat(entry.getPostings()).hasSize(2);
    }

    @Test
    void unbalancedPostingsAreRejected() {
        UUID accountA = UUID.randomUUID();
        UUID accountB = UUID.randomUUID();

        assertThatThrownBy(() -> new JournalEntry(
                        "Suspicious entry",
                        "transfer-999",
                        List.of(
                                new Posting(accountA, Direction.DEBIT, new BigDecimal("50.00")),
                                new Posting(accountB, Direction.CREDIT, new BigDecimal("49.99")))))
                .isInstanceOf(UnbalancedJournalEntryException.class)
                .hasMessageContaining("Debits");
    }

    @Test
    void aSinglePostingIsAlwaysRejectedEvenIfSomehowBalanced() {
        UUID accountA = UUID.randomUUID();

        assertThatThrownBy(() -> new JournalEntry(
                        "One-sided entry",
                        "transfer-000",
                        List.of(new Posting(accountA, Direction.DEBIT, BigDecimal.ZERO))))
                .isInstanceOf(UnbalancedJournalEntryException.class);
    }

    @Test
    void threeLeggedEntriesCanBalanceToo() {
        // Not every real-world entry is a simple two-account transfer — a
        // deposit that splits into principal + fee, for example, needs
        // three postings that still have to balance overall.
        UUID payer = UUID.randomUUID();
        UUID payee = UUID.randomUUID();
        UUID feeAccount = UUID.randomUUID();

        JournalEntry entry = new JournalEntry(
                "Payment with fee",
                "txn-1",
                List.of(
                        new Posting(payer, Direction.DEBIT, new BigDecimal("100.00")),
                        new Posting(payee, Direction.CREDIT, new BigDecimal("97.00")),
                        new Posting(feeAccount, Direction.CREDIT, new BigDecimal("3.00"))));

        assertThat(entry.getPostings()).hasSize(3);
    }
}
