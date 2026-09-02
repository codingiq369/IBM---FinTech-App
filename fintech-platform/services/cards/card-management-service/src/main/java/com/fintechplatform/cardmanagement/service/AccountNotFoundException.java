package com.fintechplatform.cardmanagement.service;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(UUID id) {
        super("No account found with id " + id);
    }
}
