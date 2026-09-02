package com.fintechplatform.cardmanagement.service;

import java.util.UUID;

/** The account exists but isn't in a state a card can be issued against
 * (currently: anything other than ACTIVE). Kept distinct from
 * {@link AccountNotFoundException} so callers get a 422, not a 404 — the
 * request named a real account, it just isn't eligible right now. */
public class AccountNotEligibleException extends RuntimeException {
    public AccountNotEligibleException(UUID accountId, String accountStatus) {
        super("Account " + accountId + " is not eligible for a new card (status: " + accountStatus + ")");
    }
}
