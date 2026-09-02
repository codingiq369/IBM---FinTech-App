package com.fintechplatform.cardmanagement.domain;

/**
 * ISSUED -&gt; ACTIVE is a one-way trip through {@code POST /api/cards/{id}/activate}.
 * ACTIVE -&gt; BLOCKED is reversible in principle (customer reports the card
 * found again) but this slice only exposes the block direction; unblocking
 * is left as a follow-up (see the FRD's out-of-scope section). CLOSED is
 * terminal — a closed card can never authorize a purchase again.
 */
public enum CardStatus {
    ISSUED,
    ACTIVE,
    BLOCKED,
    CLOSED
}
