package com.fintechplatform.customer.service;

public class DuplicateCustomerException extends RuntimeException {
    public DuplicateCustomerException(String email) {
        super("A customer with email '" + email + "' already exists");
    }
}
