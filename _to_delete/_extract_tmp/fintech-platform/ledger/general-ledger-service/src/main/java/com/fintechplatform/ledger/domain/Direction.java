package com.fintechplatform.ledger.domain;

/**
 * Which side of a posting this is. We model every ledger account as
 * "credit-normal" — like a real bank's core ledger treats a customer's
 * deposit account as a liability the bank owes the customer: a CREDIT
 * increases the balance (a deposit), a DEBIT decreases it (a withdrawal or
 * an outgoing transfer). That's the opposite of how a personal expense
 * tracker usually shows debits/credits, which trips people up the first
 * time they build a ledger — worth remembering while reading this code.
 */
public enum Direction {
    DEBIT,
    CREDIT
}
