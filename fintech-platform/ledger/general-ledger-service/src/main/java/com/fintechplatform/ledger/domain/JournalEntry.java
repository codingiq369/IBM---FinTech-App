package com.fintechplatform.ledger.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A single, atomic movement of money, made up of two or more
 * {@link Posting}s. This is where double-entry bookkeeping is enforced as an
 * invariant of the domain model itself, not just a validation step someone
 * could forget to call: it is impossible to construct a {@code JournalEntry}
 * whose debits and credits don't sum to the same amount. If the constructor
 * doesn't throw, the entry balances — full stop.
 */
@Entity
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String description;

    /** Free-form reference to whatever business event caused this entry —
     * e.g. a transfers-service Transfer id. Not interpreted by the ledger. */
    @Column
    private String transactionReference;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Posting> postings;

    protected JournalEntry() {
        // JPA
    }

    public JournalEntry(String description, String transactionReference, List<Posting> postings) {
        if (postings == null || postings.size() < 2) {
            throw new UnbalancedJournalEntryException("A journal entry needs at least two postings (one debit, one credit)");
        }

        BigDecimal totalDebits = sumByDirection(postings, Direction.DEBIT);
        BigDecimal totalCredits = sumByDirection(postings, Direction.CREDIT);
        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new UnbalancedJournalEntryException(
                    "Debits (" + totalDebits + ") must equal credits (" + totalCredits + ") but they do not");
        }

        this.description = description;
        this.transactionReference = transactionReference;
        this.createdAt = Instant.now();
        this.postings = new ArrayList<>(postings);
        this.postings.forEach(posting -> posting.assignTo(this));
    }

    private static BigDecimal sumByDirection(List<Posting> postings, Direction direction) {
        return postings.stream()
                .filter(posting -> posting.getDirection() == direction)
                .map(Posting::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Posting> getPostings() {
        return Collections.unmodifiableList(postings);
    }
}
