package com.fintechplatform.accounts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A bank account. Note there is no balance field here on purpose — balances
 * are derived from ledger-service's journal entries, never stored twice.
 * {@code ledgerAccountId} is the join key between this record and the
 * ledger's own account record.
 *
 * <p>The id is assigned by the caller (a random UUID) rather than generated
 * by the database, because {@link com.fintechplatform.accounts.service.AccountService}
 * needs a stable account id to hand to ledger-service <em>before</em> this
 * row is ever persisted — see that class for why.
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID ledgerAccountId;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected Account() {
        // JPA
    }

    public Account(UUID id, UUID customerId, UUID ledgerAccountId, String accountNumber, AccountType accountType, String currency) {
        this.id = id;
        this.customerId = customerId;
        this.ledgerAccountId = ledgerAccountId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.currency = currency;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getLedgerAccountId() {
        return ledgerAccountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
}
