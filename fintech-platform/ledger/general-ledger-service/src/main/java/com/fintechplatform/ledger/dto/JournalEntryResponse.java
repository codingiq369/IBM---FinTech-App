package com.fintechplatform.ledger.dto;

import com.fintechplatform.ledger.domain.JournalEntry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JournalEntryResponse(
        UUID id, String description, String transactionReference, Instant createdAt, List<PostingResponse> postings) {

    public static JournalEntryResponse from(JournalEntry entry) {
        return new JournalEntryResponse(
                entry.getId(),
                entry.getDescription(),
                entry.getTransactionReference(),
                entry.getCreatedAt(),
                entry.getPostings().stream().map(PostingResponse::from).toList());
    }
}
