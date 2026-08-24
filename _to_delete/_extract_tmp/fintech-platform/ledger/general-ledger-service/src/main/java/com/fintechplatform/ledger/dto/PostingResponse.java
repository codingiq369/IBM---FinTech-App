package com.fintechplatform.ledger.dto;

import com.fintechplatform.ledger.domain.Posting;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PostingResponse(UUID id, UUID ledgerAccountId, String direction, BigDecimal amount, Instant createdAt) {
    public static PostingResponse from(Posting posting) {
        return new PostingResponse(
                posting.getId(), posting.getLedgerAccountId(), posting.getDirection().name(), posting.getAmount(), posting.getCreatedAt());
    }
}
