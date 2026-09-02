package com.fintechplatform.notifications.event;

import java.util.UUID;

/**
 * What this service actually needs out of a {@code transaction-events}
 * message, independent of which producer sent it. Deliberately not a copy
 * of {@code TransferCompletedEvent} or {@code CardAuthorizationApprovedEvent}
 * — this service doesn't own either of those types (see this codebase's
 * long-standing rule that services never share domain types across a
 * network boundary; ADR-0003 restates it for this event bus specifically),
 * and only needs four things from any event on this topic: an id to
 * dedupe on, a type to label it, an entity to reference, and something
 * readable to show a human. {@link TransactionEventParser} is what builds
 * one of these from the raw JSON.
 */
public record ParsedTransactionEvent(UUID eventId, String eventType, UUID referenceId, String summary) {}
