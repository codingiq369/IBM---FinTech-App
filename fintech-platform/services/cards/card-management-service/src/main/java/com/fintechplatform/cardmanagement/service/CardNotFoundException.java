package com.fintechplatform.cardmanagement.service;

import java.util.UUID;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(UUID id) {
        super("No card found with id " + id);
    }
}
