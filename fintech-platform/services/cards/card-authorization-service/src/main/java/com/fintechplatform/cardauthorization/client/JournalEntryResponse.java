package com.fintechplatform.cardauthorization.client;

import java.time.Instant;
import java.util.UUID;

public record JournalEntryResponse(UUID id, String description, String transactionReference, Instant createdAt) {}
