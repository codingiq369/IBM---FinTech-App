package com.fintechplatform.transfers.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A request to move money from one account to another, and everything that
 * happened while we tried. Starts life as PENDING the moment we've validated
 * both accounts, then transitions exactly once to either COMPLETED (the
 * ledger accepted the journal entry) or FAILED (it didn't, or was
 * unreachable) — a Transfer is never silently discarded on failure.
 */
@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID sourceAccountId;

    @Column(nullable = false)
    private UUID destinationAccountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    /** The ledger-service JournalEntry id, once the transfer completes. */
    @Column
    private UUID journalEntryReference;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Transfer() {
        // JPA
    }

    public Transfer(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount, String currency) {
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.currency = currency;
        this.status = TransferStatus.PENDING;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void markCompleted(UUID journalEntryReference) {
        this.status = TransferStatus.COMPLETED;
        this.journalEntryReference = journalEntryReference;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.status = TransferStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public UUID getJournalEntryReference() {
        return journalEntryReference;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
