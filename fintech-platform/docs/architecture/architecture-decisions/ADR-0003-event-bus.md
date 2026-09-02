# ADR-0003: A first, minimal step into event-driven architecture

**Status:** Accepted
**Date:** 2026-09-02
**Deciders:** Platform Engineering

## Context

Every inter-service interaction in this platform so far is synchronous
HTTP: `internal-transfer-service` calls `accounts-service` and
`ledger-service` directly and waits for a response; `card-authorization-service`
does the same. That's the right choice for the request/response part of
each flow — the caller genuinely needs an answer before it can tell its
own caller whether the transfer or purchase succeeded (see
`docs/architecture/vertical-slice.md`'s "Simplifications made on purpose").
It is the wrong choice for anything that merely wants to *know* that a
transfer completed or a purchase was approved without being on the
critical path of deciding it — a notification, a fraud check, a nightly
reporting job. Coupling those to the transfer or authorization flow via
another synchronous call would mean a slow or down notifications system
could make transfers fail, which is backwards: nobody should be able to
block a customer's money movement because an email couldn't be sent.

`messaging/kafka` has existed as empty scaffolding since the platform's
first commit, named for exactly this purpose (see the root README's
"Everything else" section and item 3 of vertical-slice.md's "What to build
next"). This ADR is the smallest possible step into it: one event source
in each of the two existing verticals, one consumer, no infrastructure
this repository doesn't already have a place for.

## Decision

**Publish two events, from the two places a customer-visible outcome is
already decided, onto one shared topic.**

- `internal-transfer-service` publishes `TransferCompleted` (never
  `TransferFailed` — see "What's deliberately out of scope" below) after
  `TransferExecutionService` has already committed the Transfer as
  COMPLETED.
- `card-authorization-service` publishes `CardAuthorizationApproved`
  (never a decline) after `CardAuthorizationExecutionService` has already
  committed the CardAuthorization as APPROVED.
- Both publish to the same Kafka topic, **`transaction-events`** — one of
  the seven topic names the scaffold already named
  (`messaging/kafka/topics/transaction-events.yaml`, previously empty,
  filled in alongside this ADR) rather than a new topic invented for this
  sprint. A transfer and a card purchase are, at the ledger, the same kind
  of fact: a balanced journal entry was posted on a customer's behalf.
  Each event's `eventType` field (`"TransferCompleted"` /
  `"CardAuthorizationApproved"`) is how a consumer tells them apart; there
  is no shared Java type between the two publishers, only the same shape
  of record (`eventId`, `eventType`, the domain ids involved, amount,
  currency, `journalEntryReference`, `occurredAt`) independently defined
  in each service's own `event` package, consistent with this codebase's
  existing rule that services never share domain types across a network
  boundary.
- A new, minimal consumer, `services/notifications/notification-orchestrator`,
  subscribes to `transaction-events` and logs each event it receives —
  literally the entire feature. See ADR context below for why logging,
  not sending a real notification, is the right scope for this step.
- Publishing is **best-effort and fire-and-forget**, deliberately outside
  the transaction that saved the Transfer/CardAuthorization. See
  `TransferEventPublisher` and `CardAuthorizationEventPublisher`: both
  catch every exception a send can produce (synchronous client errors and
  asynchronous send failures alike) and only log a warning. A customer's
  transfer or purchase is already a done deal, committed to
  `transfers_db` / `card_authorization_db`, by the time either publisher
  is even called — nothing about Kafka being slow, unreachable, or
  entirely absent (e.g. a developer running one service standalone
  without docker-compose) can change that outcome or make the API call
  fail.
- No schema registry, no Avro, no Protobuf. Events are plain JSON strings
  (`ObjectMapper.writeValueAsString`), matching this platform's existing
  "no unnecessary infrastructure" bias (compare: REST + Jackson
  everywhere else, no gRPC).
- Serialization is a plain string key (the source entity's id — the
  transfer id or the card authorization id) and a plain string value,
  using Spring Kafka's `KafkaTemplate<String, String>` with
  `StringSerializer` on both sides — no custom serializer to write or
  maintain.

## What's deliberately out of scope

- **No `TransferFailed` or `CardAuthorizationDeclined` events.** A failed
  transfer or a declined purchase is already a fully successful,
  fully-recorded API response (see both services' own domain docs) — it
  doesn't need a downstream system reacting to it yet. This may change
  once there's an actual consumer for it (e.g. a fraud service that wants
  to see declines, not just approvals); adding a second event type to the
  same topic is a small, additive change when that need is real.
- **Dedup is a unique constraint, not a full idempotent-consumption
  design.** At-least-once delivery means `notification-orchestrator` can
  see the same event twice (e.g. after a consumer-group rebalance). It
  guards against that the cheap way: a unique constraint on `event_id` in
  its own `notification_records` table (`NotificationService.recordEvent`
  checks first, the constraint is the real backstop against a race), so a
  redelivered message produces one row, not two. That's sufficient today
  because writing that row *is* this consumer's only side effect — the
  moment `notification-orchestrator` does anything with an external side
  effect (sending an email, pushing to a device), a unique-row check
  stops being enough on its own (the row and the side effect can still
  diverge if the process crashes between them) and it needs the fuller
  outbox-style pattern described in `docs/domains/notifications.md`.
- **No transactional outbox.** `TransferEventPublisher` is called after
  the transfer's COMPLETED status is already committed in its own
  transaction (`TransferExecutionService`, `REQUIRES_NEW`) — there is a
  real, if narrow, window where the transfer is saved but the process
  crashes before the Kafka send happens, silently dropping the event. An
  outbox table (write the event to `transfers_db` in the same transaction
  as the Transfer, and a separate poller publishes it to Kafka and marks
  it sent) closes that window, at the cost of a poller process and a table
  per publishing service. Not worth it yet for a consumer that only logs;
  worth revisiting the moment a real business process depends on
  delivery.
- **No schema registry / compatibility enforcement.** Fine for two
  producers and one internal consumer, all in the same repository,
  deployed together. The first time an external team or a different
  deployable needs to consume these events, this decision should be
  revisited.
- **Single-node Kafka (KRaft mode, no ZooKeeper), no replication.**
  Matches the rest of `deployment/docker/docker-compose.yml` — one
  Postgres instance, no HA anywhere — a local/demo topology, not a
  production one. See `docs/operations/deployment.md` for what a
  production Kafka topology would need (multiple brokers, replication
  factor > 1, a managed service like MSK/Confluent Cloud rather than a
  self-hosted single node).

## Consequences

**What this buys:** `internal-transfer-service` and
`card-authorization-service` gained a way to announce what they did
without knowing anything about who's listening — today,
`notification-orchestrator`; tomorrow, fraud scoring, reporting, or
anything else that just wants to observe committed transactions. Neither
publishing service took on a hard dependency on Kafka: every existing
test, and the services themselves, work identically whether Kafka is
running or not (a send failure is logged, not thrown). This is the proof
this ADR set out to get, matching ADR-0010's pattern of proving out one
new architectural capability with the smallest possible real example
before committing to it everywhere.

**What this costs, on purpose:** the "no outbox" and "no dedup" gaps above
are real and are not hidden — a transfer or purchase can complete with its
event silently never arriving (Kafka down at the wrong instant), and a
restarted consumer can process the same event twice. Both are acceptable
for a consumer whose only behavior today is a log line, and both are
named here precisely so nobody mistakes this for a delivery-guaranteed
event bus before the guarantees are actually built.

## Alternatives considered

- **A topic per producing service** (`transfer-events`,
  `card-authorization-events`). More conventional in a large Kafka
  deployment where topic ownership tracks service ownership. Rejected for
  this step: with exactly one consumer that wants both event types, two
  topics would only mean `notification-orchestrator` subscribes to two
  topic names instead of one field-based discriminator, for no present
  benefit — and it would leave two of the scaffold's seven pre-named
  topic placeholders unfilled while inventing two new ones with no
  scaffold precedent. Revisit this the moment a consumer wants
  `TransferCompleted` but not `CardAuthorizationApproved` (or vice versa)
  — splitting a topic later is a normal, low-drama migration.
- **Publish onto `notification-events` instead of `transaction-events`.**
  Rejected: `notification-events` reads as the *output* of a
  notification-worthy decision (e.g. "an email was sent"), not the raw
  domain fact a decision gets made from. `transaction-events` is the
  input signal; `notification-events` is left as scaffolding for whatever
  `notification-orchestrator` itself might one day emit downstream (a
  delivery receipt, for instance).
- **A shared `messaging/kafka` Java library with the event types.**
  Rejected, consistent with this platform's existing no-shared-domain-types
  rule (see ADR-0001/ADR-0002 and every `*Client` wrapper in the
  codebase): each service defines its own copy of the event shape it
  produces or consumes. A few duplicated fields is a small price for never
  needing two services to agree on a release train for a shared JAR.
- **Synchronous webhook callback instead of Kafka.** Rejected: reintroduces
  exactly the coupling this ADR exists to avoid (a slow or down
  notifications endpoint could make `POST /api/transfers` hang or fail).
- **Do nothing until a second event-driven use case exists.** Reasonable,
  but the scaffold already commits to Kafka as the platform's intended
  answer here (see the seven pre-named topics), and vertical-slice.md's
  own roadmap already named this as the next thing to prove out. Better to
  learn the real shape of "one event, one consumer" now, with two
  low-stakes events, than to design it for the first time under pressure
  from a use case that actually needs strong delivery guarantees.
