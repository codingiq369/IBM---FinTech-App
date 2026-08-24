package com.fintechplatform.transfers.service;

/** Covers everything wrong with a transfer request that we can tell before
 * even trying to move money: same-account transfer, mismatched currencies,
 * a non-active account. These never produce a Transfer record — the request
 * itself was invalid, there was nothing to attempt. */
public class InvalidTransferException extends RuntimeException {
    public InvalidTransferException(String message) {
        super(message);
    }
}
