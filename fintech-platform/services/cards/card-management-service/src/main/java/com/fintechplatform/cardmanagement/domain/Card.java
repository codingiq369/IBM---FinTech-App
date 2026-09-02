package com.fintechplatform.cardmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

/**
 * A debit card linked to exactly one bank account. Like {@code Account}, the
 * id is minted by the caller (a random UUID) rather than left to the
 * database, for the same reason: {@link com.fintechplatform.cardmanagement.service.CardService}
 * needs a stable id up front and this keeps issuance to a single insert.
 *
 * <p>There is no PIN, CVV, or full PAN stored here — {@code cardNumberLastFour}
 * plus a synthetic, non-reversible {@code cardNumberMasked} display value is
 * enough for this slice's demo purposes. A real issuer would tokenize the
 * PAN through a dedicated card-tokenization-service and a PCI-DSS scoped
 * vault; see docs/domains/cards.md.
 */
@Entity
@Table(name = "cards")
public class Card {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false, unique = true, length = 19)
    private String cardNumberMasked;

    @Column(nullable = false, length = 4)
    private String cardNumberLastFour;

    @Column(nullable = false, length = 120)
    private String cardholderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CardType cardType;

    @Column(nullable = false)
    private int expiryMonth;

    @Column(nullable = false)
    private int expiryYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CardStatus status;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal dailyPurchaseLimit;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant activatedAt;

    @Column
    private Instant blockedAt;

    protected Card() {
        // JPA
    }

    public Card(
            UUID id,
            UUID accountId,
            UUID customerId,
            String cardNumberMasked,
            String cardNumberLastFour,
            String cardholderName,
            CardType cardType,
            BigDecimal dailyPurchaseLimit) {
        this.id = id;
        this.accountId = accountId;
        this.customerId = customerId;
        this.cardNumberMasked = cardNumberMasked;
        this.cardNumberLastFour = cardNumberLastFour;
        this.cardholderName = cardholderName;
        this.cardType = cardType;
        this.dailyPurchaseLimit = dailyPurchaseLimit;
        this.status = CardStatus.ISSUED;
        YearMonth expiry = YearMonth.now().plusYears(4);
        this.expiryMonth = expiry.getMonthValue();
        this.expiryYear = expiry.getYear();
        this.createdAt = Instant.now();
    }

    public void activate() {
        if (status != CardStatus.ISSUED) {
            throw new IllegalStateException("Only an ISSUED card can be activated (was " + status + ")");
        }
        this.status = CardStatus.ACTIVE;
        this.activatedAt = Instant.now();
    }

    public void block() {
        if (status == CardStatus.CLOSED) {
            throw new IllegalStateException("A CLOSED card cannot be blocked");
        }
        this.status = CardStatus.BLOCKED;
        this.blockedAt = Instant.now();
    }

    public boolean isActive() {
        return status == CardStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }

    public String getCardNumberLastFour() {
        return cardNumberLastFour;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public CardType getCardType() {
        return cardType;
    }

    public int getExpiryMonth() {
        return expiryMonth;
    }

    public int getExpiryYear() {
        return expiryYear;
    }

    public CardStatus getStatus() {
        return status;
    }

    public BigDecimal getDailyPurchaseLimit() {
        return dailyPurchaseLimit;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getActivatedAt() {
        return activatedAt;
    }

    public Instant getBlockedAt() {
        return blockedAt;
    }
}
