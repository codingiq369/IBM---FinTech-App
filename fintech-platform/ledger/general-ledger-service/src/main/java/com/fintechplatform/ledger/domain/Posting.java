package com.fintechplatform.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * One leg of a journal entry: money moving on one side of one account.
 * A posting never exists on its own — it always belongs to a
 * {@link JournalEntry}, and that entry's postings must balance.
 *
 * <p>Deliberately not a JPA association to {@link LedgerAccount}: a posting
 * only needs the account's id to be queried and summed, and keeping it a
 * plain column avoids accidentally loading (or cascading changes into)
 * accounts just by looking at postings.
 */
@Entity
@Table(name = "postings")
public class Posting {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @Column(nullable = false)
    private UUID ledgerAccountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant createdAt;

    protected Posting() {
        // JPA
    }

    public Posting(UUID ledgerAccountId, Direction direction, BigDecimal amount) {
        this.ledgerAccountId = ledgerAccountId;
        this.direction = direction;
        this.amount = amount;
        this.createdAt = Instant.now();
    }

    void assignTo(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public UUID getId() {
        return id;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public UUID getLedgerAccountId() {
        return ledgerAccountId;
    }

    public Direction getDirection() {
        return direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
