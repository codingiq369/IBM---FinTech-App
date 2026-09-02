# notification-orchestrator

Consumes `transaction-events` from Kafka and records each event as a row
in its own database — the smallest possible taste of event-driven
architecture in this platform. See
[ADR-0003](../../../docs/architecture/architecture-decisions/ADR-0003-event-bus.md)
for why it's shaped this way, and
[`docs/domains/notifications.md`](../../../docs/domains/notifications.md)
for what "notify" does and does not mean at this stage.

## What it does

- Listens to the `transaction-events` Kafka topic for two event types:
  `TransferCompleted` (published by `internal-transfer-service`) and
  `CardAuthorizationApproved` (published by `card-authorization-service`).
- Parses each message and saves a `NotificationRecord` — an append-only
  audit row with a human-readable summary, deduplicated by the event's own
  `eventId` so a redelivered message doesn't produce a second row.
- Exposes exactly one HTTP endpoint, `GET /api/notifications`, returning
  the 50 most recent records, newest first. There is no `POST` — the only
  way a record is created is a Kafka message arriving.

## What it deliberately does not do (yet)

- **Doesn't send an email, SMS, or push notification.** "Notify" here
  means "make visible in a feed a human or another service can read," not
  "deliver to a person's inbox." That's the natural next step once a real
  delivery channel is worth building — see `docs/domains/notifications.md`.
- **Doesn't guarantee delivery.** Publishing on the producer side is
  best-effort and fire-and-forget (see `TransferEventPublisher` /
  `CardAuthorizationEventPublisher` in the producing services) — an event
  can be lost if Kafka is unreachable at the moment a transfer or purchase
  completes. See ADR-0003's "What's deliberately out of scope."

## Running it standalone

Like every other service in this platform, it's a self-contained Maven
project:

```bash
cd services/notifications/notification-orchestrator
mvn spring-boot:run
```

It needs a reachable Postgres (`DB_URL`, default
`localhost:5432/notifications_db`) and a reachable Kafka broker
(`KAFKA_BOOTSTRAP_SERVERS`, default `localhost:9092`) — both provided by
`docker compose up` from `deployment/docker`, which is the easiest way to
run it alongside the two services that publish to it.

## Running the tests

```bash
mvn test
```

`TransactionEventParserTest` covers turning both event shapes' raw JSON
into a `ParsedTransactionEvent`, plus malformed/unrecognized payloads.
`NotificationServiceTest` covers the dedup-by-eventId behavior. Neither
test starts a real Kafka broker or Postgres — see the note in the root
README on why the Java services in this repository weren't compiled or
test-run before being handed off.
