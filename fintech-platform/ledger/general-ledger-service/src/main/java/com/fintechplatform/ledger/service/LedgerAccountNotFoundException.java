package com.fintechplatform.ledger.service;

import java.util.UUID;

public class LedgerAccountNotFoundException extends RuntimeException {
    public LedgerAccountNotFoundException(UUID id) {
        super("No ledger account found with id " + id);
    }
}
