package com.fintechplatform.transfers.service;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(UUID accountId) {
        super("No account found with id " + accountId);
    }
}
