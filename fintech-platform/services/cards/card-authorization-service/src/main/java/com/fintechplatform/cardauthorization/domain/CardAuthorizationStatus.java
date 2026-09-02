package com.fintechplatform.cardauthorization.domain;

/** Every authorization request ends up in exactly one of these — there is
 * no PENDING here, unlike {@code TransferStatus}, because the whole decision
 * (limit check, ledger posting) happens synchronously within one request;
 * see {@code CardAuthorizationService} for why that's a safe simplification
 * for this slice. */
public enum CardAuthorizationStatus {
    APPROVED,
    DECLINED
}
