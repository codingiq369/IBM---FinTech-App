package com.fintechplatform.transfers.client;

import java.math.BigDecimal;
import java.util.UUID;

/** Mirrors ledger-service's PostingRequest. {@code direction} must be
 * exactly "DEBIT" or "CREDIT" to match the enum ledger-service deserializes into. */
public record PostingRequest(UUID ledgerAccountId, String direction, BigDecimal amount) {}
