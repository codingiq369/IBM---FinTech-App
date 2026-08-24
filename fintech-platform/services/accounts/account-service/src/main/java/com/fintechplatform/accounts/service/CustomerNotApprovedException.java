package com.fintechplatform.accounts.service;

import java.util.UUID;

public class CustomerNotApprovedException extends RuntimeException {
    public CustomerNotApprovedException(UUID customerId) {
        super("Customer " + customerId + " is not an approved customer; cannot open an account for them");
    }
}
