package com.fintechplatform.customer.service;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(UUID id) {
        super("No customer found with id " + id);
    }
}
