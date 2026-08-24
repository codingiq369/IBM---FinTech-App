package com.fintechplatform.accounts.service;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(UUID id) {
        super("No account found with id " + id);
    }
}
