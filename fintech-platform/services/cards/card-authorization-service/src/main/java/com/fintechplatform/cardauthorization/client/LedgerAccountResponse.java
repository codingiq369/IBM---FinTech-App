package com.fintechplatform.cardauthorization.client;

import java.time.Instant;
import java.util.UUID;

public record LedgerAccountResponse(UUID id, String ownerReference, String currency, Instant createdAt) {}
