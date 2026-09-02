package com.fintechplatform.cardauthorization.domain;

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
 * One attempt to authorize a card purchase, and the outcome. Every attempt
 * is recorded — a decline is not an error response, it's a normal, expected
 * outcome a real card network returns constantly (insufficient funds, an
 * inactive card, a limit hit) and the cardholder's statement needs to show
 * declines too, not just approvals.
 */
@Entity
@Table(name = "card_authorizations")
public class CardAuthorization {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID cardId;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false, length = 120)
    private String merchantName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CardAuthorizationStatus status;

    /** The ledger-service JournalEntry id, only set when APPROVED. */
    @Column
    private UUID journalEntryReference;

    @Column(length = 500)
    private String declineReason;

    @Column(nullable = false)
    private Instant createdAt;

    protected CardAuthorization() {
        // JPA
    }

    private CardAuthorization(UUID cardId, UUID accountId, String merchantName, BigDecimal amount, String currency) {
        this.cardId = cardId;
        this.accountId = accountId;
        this.merchantName = merchantName;
        this.amount = amount;
        this.currency = currency;
        this.createdAt = Instant.now();
    }

    /** A decline decided locally — the card wasn't active, or the daily
     * limit would be exceeded — before ledger-service was ever called. */
    public static CardAuthorization declined(UUID cardId, UUID accountId, String merchantName, BigDecimal amount, String currency, String reason) {
        CardAuthorization authorization = new CardAuthorization(cardId, accountId, merchantName, amount, currency);
        authorization.status = CardAuthorizationStatus.DECLINED;
        authorization.declineReason = reason;
        return authorization;
    }

    /** Still undecided — about to be handed to ledger-service, which will
     * decide APPROVED or DECLINED (e.g. insufficient funds). */
    public static CardAuthorization pendingLedgerDecision(UUID cardId, UUID accountId, String merchantName, BigDecimal amount, String currency) {
        return new CardAuthorization(cardId, accountId, merchantName, amount, currency);
    }

    public void markApproved(UUID journalEntryReference) {
        this.status = CardAuthorizationStatus.APPROVED;
        this.journalEntryReference = journalEntryReference;
    }

    public void markDeclined(String reason) {
        this.status = CardAuthorizationStatus.DECLINED;
        this.declineReason = reason;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCardId() {
        return cardId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public CardAuthorizationStatus getStatus() {
        return status;
    }

    public UUID getJournalEntryReference() {
        return journalEntryReference;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
