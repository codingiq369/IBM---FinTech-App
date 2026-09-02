package com.fintechplatform.notifications.event;

/** The payload didn't parse as JSON, or its {@code eventType} isn't one
 * this service knows how to summarize. Thrown so the listener has one
 * place to catch and log rather than letting a malformed or unexpected
 * message crash the consumer thread. */
public class UnrecognizedEventException extends RuntimeException {

    public UnrecognizedEventException(String message) {
        super(message);
    }

    public UnrecognizedEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
