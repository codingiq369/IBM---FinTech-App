package com.fintechplatform.transfers.client;

import java.time.Instant;
import java.util.UUID;

/** Mirrors just the fields transfers-service cares about from ledger-service's
 * JournalEntryResponse; extra fields in the real response (like the posting
 * list) are simply ignored on deserialization. */
public record JournalEntryResponse(UUID id, String description, String transactionReference, Instant createdAt) {}
