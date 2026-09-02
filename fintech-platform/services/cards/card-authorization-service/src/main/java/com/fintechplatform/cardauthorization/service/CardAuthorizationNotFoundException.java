package com.fintechplatform.cardauthorization.service;

import java.util.UUID;

public class CardAuthorizationNotFoundException extends RuntimeException {
    public CardAuthorizationNotFoundException(UUID id) {
        super("No card authorization found with id " + id);
    }
}
