package com.fintechplatform.cardauthorization.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * The card network's own ledger account for one currency — the credit leg
 * of every approved purchase, matching the debit against the cardholder's
 * account. One row per currency, created lazily the first time that
 * currency is needed. See ADR-0010 for why this exists instead of a full
 * merchant-settlement domain.
 */
@Entity
@Table(name = "clearing_accounts")
public class ClearingAccount {

    @Id
    @Column(length = 3)
    private String currency;

    @Column(nullable = false, unique = true)
    private UUID ledgerAccountId;

    @Column(nullable = false)
    private Instant createdAt;

    protected ClearingAccount() {
        // JPA
    }

    public ClearingAccount(String currency, UUID ledgerAccountId) {
        this.currency = currency;
        this.ledgerAccountId = ledgerAccountId;
        this.createdAt = Instant.now();
    }

    public String getCurrency() {
        return currency;
    }

    public UUID getLedgerAccountId() {
        return ledgerAccountId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
