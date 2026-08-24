package com.fintechplatform.ledger.dto;

import com.fintechplatform.ledger.domain.LedgerAccount;
import java.time.Instant;
import java.util.UUID;

public record LedgerAccountResponse(UUID id, UUID ownerReference, String currency, Instant createdAt) {
    public static LedgerAccountResponse from(LedgerAccount account) {
        return new LedgerAccountResponse(account.getId(), account.getOwnerReference(), account.getCurrency(), account.getCreatedAt());
    }
}
