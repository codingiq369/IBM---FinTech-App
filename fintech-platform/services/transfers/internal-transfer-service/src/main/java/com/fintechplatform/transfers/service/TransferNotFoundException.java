package com.fintechplatform.transfers.service;

import java.util.UUID;

public class TransferNotFoundException extends RuntimeException {
    public TransferNotFoundException(UUID id) {
        super("No transfer found with id " + id);
    }
}
