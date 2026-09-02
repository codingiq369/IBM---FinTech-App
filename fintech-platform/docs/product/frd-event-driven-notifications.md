# Functional Requirements Document — Event-Driven Notifications (Foundation)

**Version:** 1.0 · **Date:** 2026-09-02 · **Author:** Platform Engineering · **Status:** Approved

Filled from `templates/frd-template.md`. Traces to the PRD at
[`prd-event-driven-notifications.md`](prd-event-driven-notifications.md).

## 1. Introduction

This FRD turns the PRD's goals into the concrete, testable system behavior
implemented across `internal-transfer-service`, `card-authorization-service`,
and the new `notification-orchestrator`. Each requirement below is
implemented and covered by an automated test — see the "Covered by" column
and each service's README.

## 2. Scope

The `event` package added to `internal-transfer-service` and
`card-authorization-service`; the new
`services/notifications/notification-orchestrator` service, its REST API,
and its Postgres schema; the Kafka broker and topic added to
`deployment/docker/docker-compose.yml` and
`messaging/kafka/topics/transaction-events.yaml`; the new gateway route;
and the Activity page in `apps/web-banking`. Does not cover any other
folder under `services/notifications/`, which remains scaffolding, or any
change to either publishing service's existing decision logic.

## 3. Functional Requirements

### FR-1: Publish TransferCompleted

- **Description:** After a Transfer is committed as COMPLETED,
  `internal-transfer-service` publishes a `TransferCompleted` event.
- **Inputs:** The completed `Transfer` (id, source/destination account
  ids, amount, currency, journal entry reference).
- **Outputs:** A JSON message on the `transaction-events` Kafka topic,
  keyed by the transfer id.
- **Business rules:** Only a COMPLETED transfer is published; a FAILED
  transfer never is (see PRD Non-Goals). Publishing happens after, and
  outside, the transaction that saved the Transfer as COMPLETED.
- **Acceptance criteria:**
  - A transfer that completes successfully results in exactly one
    `publishTransferCompleted` call with that transfer's data.
  - A transfer that ends FAILED never triggers a publish call.
- **Covered by:** `TransferServiceTest` (`aCompletedTransferIsPublishedAsATransferCompletedEvent`,
  `aFailedTransferIsNeverPublished`).
- **Traces to:** PR-1.

### FR-2: Publish CardAuthorizationApproved

- **Description:** After a CardAuthorization is committed as APPROVED,
  `card-authorization-service` publishes a `CardAuthorizationApproved`
  event.
- **Inputs:** The approved `CardAuthorization` (id, card id, account id,
  merchant name, amount, currency, journal entry reference).
- **Outputs:** A JSON message on the `transaction-events` Kafka topic,
  keyed by the card authorization id.
- **Business rules:** Only an APPROVED authorization is published; a
  DECLINED one — whether declined locally (inactive card, over limit) or
  by the ledger (insufficient funds) — never is.
- **Acceptance criteria:**
  - An approved purchase results in exactly one
    `publishCardAuthorizationApproved` call with that authorization's data.
  - A declined purchase, at any decision point, never triggers a publish
    call.
- **Covered by:** `CardAuthorizationServiceTest` (`anApprovedPurchaseIsPublishedAsACardAuthorizationApprovedEvent`,
  `aDeclinedPurchaseHandedToTheLedgerIsNeverPublished`, plus the existing
  early-decline tests asserting the publisher is never touched).
- **Traces to:** PR-2.

### FR-3: Best-effort, non-blocking publish

- **Description:** `TransferEventPublisher` and
  `CardAuthorizationEventPublisher` catch every exception a Kafka send can
  produce — both a synchronous client error and an asynchronous send
  failure delivered via the returned future — and only log a warning.
- **Inputs:** Any failure mode of `KafkaTemplate.send(...)`: broker
  unreachable, serialization error, client misconfiguration.
- **Outputs:** The originating `POST /api/transfers` or
  `POST /api/card-authorizations` call completes exactly as it would have
  with Kafka fully healthy — same status code, same response body.
- **Business rules:** No exception from either publisher's
  `publish*` method is ever allowed to propagate to its caller.
- **Acceptance criteria:** A mocked `KafkaTemplate` that throws
  synchronously, and one whose returned future completes exceptionally,
  both leave `publishTransferCompleted` / `publishCardAuthorizationApproved`
  returning normally.
- **Covered by:** `TransferEventPublisherTest`, `CardAuthorizationEventPublisherTest`.
- **Traces to:** PR-3.

### FR-4: notification-orchestrator consumes both event types

- **Description:** `notification-orchestrator` subscribes to
  `transaction-events` and, for each message, records a
  `NotificationRecord` with a human-readable summary.
- **Inputs:** A raw JSON message from the topic.
- **Outputs:** A row in `notification_records`, or — for a malformed or
  unrecognized message — a logged warning and no row, without crashing
  the consumer.
- **Business rules:** Dispatches on the message's `eventType` field;
  `TransferCompleted` and `CardAuthorizationApproved` are the only two
  recognized types in this release.
- **Acceptance criteria:**
  - A well-formed `TransferCompleted` payload parses into a
    `ParsedTransactionEvent` referencing the transfer id, with a summary
    naming the amount, currency, and transfer id.
  - A well-formed `CardAuthorizationApproved` payload parses referencing
    the card authorization id, with a summary naming the amount,
    currency, merchant, and authorization id.
  - Malformed JSON and an unrecognized `eventType` both raise
    `UnrecognizedEventException` rather than a type-specific parse
    failure leaking out.
- **Covered by:** `TransactionEventParserTest`.
- **Traces to:** PR-4.

### FR-5: Duplicate-safe consumption

- **Description:** A `transaction-events` message redelivered (e.g. after
  a consumer-group rebalance) does not produce a second
  `NotificationRecord`.
- **Inputs:** Two deliveries of the same event (same `eventId`).
- **Outputs:** One row in `notification_records`, not two.
- **Business rules:** `NotificationService.recordEvent` checks
  `existsByEventId` before saving; the `event_id` column additionally
  carries a unique database constraint as the real backstop against a
  race between two near-simultaneous deliveries (see ADR-0003's dedup
  bullet for why this is a narrower guarantee than full idempotent
  consumption, and why that's sufficient here).
- **Acceptance criteria:** Calling `recordEvent` twice with the same
  `eventId` results in exactly one `save` call.
- **Covered by:** `NotificationServiceTest`.
- **Traces to:** PR-5.

### FR-6: Activity feed (demo UI)

- **Description:** `web-banking`'s Activity page lists the 50 most recent
  notifications, newest first, polling `GET /api/notifications` every 5
  seconds.
- **Inputs:** None from the user beyond navigating to the page; a manual
  "Refresh now" button is also available.
- **Outputs:** A table of when each event was received, its type, the
  referenced transfer/authorization id, and its summary. Not filtered by
  the current customer (see `docs/domains/notifications.md` for why).
- **Acceptance criteria:** `npm run build`, `npm run lint`, and
  `npm test` all pass with the new page included.
- **Covered by:** Full frontend build + existing unit test suite (13
  tests) run clean with this page added; no browser/e2e test for this
  page specifically, consistent with every other page in this app.
- **Traces to:** PR-6.

## 4. Non-Functional Requirements

| Category | Requirement |
| --- | --- |
| Performance | Publishing is fire-and-forget from the caller's perspective — `POST /api/transfers` and `POST /api/card-authorizations` do not wait on the Kafka send completing. |
| Resilience | Neither publishing service, nor the API calls they serve, depend on Kafka being reachable — see FR-3. |
| Availability | `notification-orchestrator` owns its own Postgres database (`notifications_db`), following ADR-0002 — its outage does not affect either publishing service or any existing flow. |
| Consistency | Best-effort, at-least-once delivery only — see ADR-0003's "What's deliberately out of scope" for the explicit gaps (no transactional outbox, dedup via a unique constraint rather than a full idempotent-consumption design). |

## 5. Use Cases / User Flows

1. A customer transfers money between two accounts (existing flow) →
   the transfer completes → `internal-transfer-service` publishes
   `TransferCompleted` → `notification-orchestrator` records it →
   the Activity page shows it within 5 seconds.
2. A customer's card purchase is approved (existing flow) →
   `card-authorization-service` publishes `CardAuthorizationApproved` →
   same downstream path as above.
3. A customer's card purchase is declined (inactive card, over limit, or
   insufficient funds) → no event is published → nothing appears in the
   Activity feed for it, consistent with PR-8 (Won't this release).
4. Kafka is down when a transfer completes → the transfer still returns
   COMPLETED to the caller exactly as before → the event is silently lost
   (see ADR-0003) → nothing appears in the Activity feed for that
   transfer, and no other part of the system is affected.

## 6. Data Requirements

- **`notification_records`** (notification-orchestrator): id, event id
  (unique), event type, reference id (the transfer or card authorization
  id), summary, raw payload, received at.
- No new tables in `internal-transfer-service` or
  `card-authorization-service` — publishing reads only fields already on
  `Transfer` / `CardAuthorization`.

## 7. Interface Requirements

- A new Kafka topic, `transaction-events`, on the shared broker (see
  `messaging/kafka/topics/transaction-events.yaml`).
- A read-only REST API on `notification-orchestrator`
  (`GET /api/notifications`), routed through `gateways/api-gateway` at
  `/api/notifications/**`. No write endpoint — the only way a record is
  created is a Kafka message arriving.
- New `NotificationsPage.tsx` in `apps/web-banking`, plus
  `api/notifications.ts` and `types/notification.ts`.

## 8. Assumptions & Dependencies

- Every service that publishes to `transaction-events` defines its own
  copy of the event shape it produces (no shared Java type across the
  network boundary), consistent with this platform's existing rule for
  REST DTOs.
- A single Kafka broker (KRaft mode, no replication) is sufficient for
  this release's demo/local-dev scope, matching the rest of
  `deployment/docker/docker-compose.yml`.

## 9. Traceability Matrix

| FRD ID | PRD ID |
| --- | --- |
| FR-1 | PR-1 |
| FR-2 | PR-2 |
| FR-3 | PR-3 |
| FR-4 | PR-4 |
| FR-5 | PR-5 |
| FR-6 | PR-6 |
