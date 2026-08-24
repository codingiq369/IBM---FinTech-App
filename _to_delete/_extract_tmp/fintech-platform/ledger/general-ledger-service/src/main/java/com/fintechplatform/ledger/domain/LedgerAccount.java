package com.fintechplatform.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * The ledger's own record of an account. {@code ownerReference} points back
 * to whatever external record opened it — in this slice, an accounts-service
 * Account id — but the ledger never calls back out to accounts-service or
 * knows anything about it beyond that id. That one-way reference is what
 * keeps the ledger reusable: it doesn't care whether the thing on the other
 * end is a checking account, a savings account, or something we haven't
 * built yet.
 */
@Entity
@Table(name = "ledger_accounts")
public class LedgerAccount {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID ownerReference;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private Instant createdAt;

    protected LedgerAccount() {
        // JPA
    }

    public LedgerAccount(UUID ownerReference, String currency) {
        this.ownerReference = ownerReference;
        this.currency = currency;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerReference() {
        return ownerReference;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
