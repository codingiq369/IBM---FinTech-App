package com.fintechplatform.cardmanagement.domain;

/**
 * Only {@link #DEBIT} is actually issuable in this slice — a debit card
 * draws directly against the linked bank account's real ledger balance,
 * which needs no new concepts beyond what accounts-service and
 * ledger-service already provide. {@link #CREDIT} is kept in the enum
 * because the domain model (a card, a status, a linked account) is the
 * right shape for it too, but issuing one needs a credit line and a
 * repayment relationship that belongs to services/lending, not here — see
 * ADR-0010 and the PRD's Out of Scope section.
 */
public enum CardType {
    DEBIT,
    CREDIT
}
