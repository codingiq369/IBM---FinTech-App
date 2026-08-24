package com.fintechplatform.ledger.service;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(UUID ledgerAccountId, BigDecimal available, BigDecimal requested) {
        super("Ledger account " + ledgerAccountId + " has insufficient funds: available " + available + ", requested " + requested);
    }
}
