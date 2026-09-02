package com.fintechplatform.cardauthorization.client;

import java.math.BigDecimal;
import java.util.UUID;

public record PostingRequest(UUID ledgerAccountId, String direction, BigDecimal amount) {}
