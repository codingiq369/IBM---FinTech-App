# Product Requirements Document — Event-Driven Notifications (Foundation)

**Version:** 1.0 · **Date:** 2026-09-02 · **Author:** Platform Engineering · **Status:** Approved

Filled from `templates/prd-template.md`. Traces to the FRD at
[`frd-event-driven-notifications.md`](frd-event-driven-notifications.md)
and to [ADR-0003](../architecture/architecture-decisions/ADR-0003-event-bus.md).

## 1. Problem Statement

Both vertical slices built so far — transfers and cards — communicate
exclusively over synchronous HTTP. That's the correct choice for the
decision-making part of each flow, but it means nothing outside
`internal-transfer-service` or `card-authorization-service` can find out a
transfer completed or a purchase was approved without those services
calling it directly and waiting on the response — coupling a customer's
money movement to the availability of every system that might care about
it (notifications, fraud detection, reporting). `messaging/kafka` has
existed as named-but-empty scaffolding since this repository's first
commit specifically to solve this, and `docs/architecture/vertical-slice.md`
named it as the next thing to prove out. This release is that proof: the
smallest real, working slice of event-driven architecture this platform
can have, built the same way the Cards slice proved out a second REST
product on the same foundations.

This is deliberately **platform/infrastructure work**, not a customer-facing
feature — there is no new button a customer clicks. The customer-visible
surface (an "Activity" feed in the demo UI) exists to make the plumbing
observable and demoable, not because a demo customer specifically asked
for a feed.

## 2. Goals

- Prove this platform can carry an event from a producing service to a
  consuming service over Kafka, in a way that never puts the producer's
  correctness or availability at risk if Kafka or the consumer is slow,
  unreachable, or entirely absent.
- Give the two services that already make a customer-visible completion
  decision (`internal-transfer-service`, `card-authorization-service`) a
  way to announce it without knowing who's listening.
- Stand up the first real consumer, so the pattern is proven end-to-end
  (publish → broker → consume → observable effect), not just "a service
  calls `kafkaTemplate.send()` and nothing reads it."
- Make the result demoable: a person watching the demo UI can perform a
  transfer or a card purchase and see it show up in an activity feed a few
  seconds later, sourced from a different service than the one that
  processed the request.

## 3. Non-Goals

- Sending an actual email, SMS, or push notification to a customer (see
  `docs/domains/notifications.md` for what "notify" means at this stage).
- Guaranteed delivery, a transactional outbox, or exactly-once semantics —
  see ADR-0003's "What's deliberately out of scope."
- A schema registry or any cross-service schema compatibility tooling.
- Events for anything other than the two already-decided outcomes named
  above (no `TransferFailed`, no `CardAuthorizationDeclined`, no events
  from any other domain).
- Migrating any existing synchronous call to async — every existing HTTP
  call in this platform stays exactly as it is.

## 4. Target Users / Personas

| Persona | Description | Primary need |
| --- | --- | --- |
| Platform engineer (next team) | Building the *next* thing that needs to react to a completed transaction (fraud scoring, reporting, real notification delivery) | A working example of "how do I consume a domain event in this platform" to extend, the same role the Cards slice played for "how do I add a synchronous domain" |
| Person watching the demo | Anyone exercising `web-banking` | Visible proof that "the platform reacted to what I just did" without needing to read service logs |

## 5. User Stories

- As a platform engineer building the next event-driven feature, I want a
  real, working example of one topic with two producers and one consumer,
  so that I don't have to make the first Kafka integration decisions from
  scratch under pressure from a specific deadline.
- As a person using the demo UI, I want to see a completed transfer or
  approved card purchase show up in an activity feed shortly after it
  happens, so that the event-driven part of the platform is something I
  can actually observe, not just something described in a doc.
- As a platform engineer, I want publishing an event to be structurally
  incapable of failing the transfer or purchase it's about, so that
  adopting Kafka anywhere in this platform never becomes a new way for a
  customer-facing request to fail.

## 6. Features & Requirements

| ID | Feature | Description | Priority | Traces to (FRD) |
| --- | --- | --- | --- | --- |
| PR-1 | Publish TransferCompleted | `internal-transfer-service` publishes an event after a transfer commits as COMPLETED | Must | FR-1 |
| PR-2 | Publish CardAuthorizationApproved | `card-authorization-service` publishes an event after a purchase commits as APPROVED | Must | FR-2 |
| PR-3 | Best-effort, non-blocking publish | A Kafka failure never fails, slows perceptibly, or changes the outcome of the originating request | Must | FR-3 |
| PR-4 | notification-orchestrator consumer | A new service consumes both event types and records each as an audit-trail row | Must | FR-4 |
| PR-5 | Duplicate-safe consumption | The same event, redelivered, does not produce a duplicate row | Must | FR-5 |
| PR-6 | Activity feed (demo UI) | `web-banking` shows the recorded notifications, auto-refreshing | Should | FR-6 |
| PR-7 | Real notification delivery (email/SMS/push) | Won't (this release) — see `docs/domains/notifications.md` | Won't (this release) | — |
| PR-8 | TransferFailed / CardAuthorizationDeclined events | Won't (this release) — no consumer needs them yet | Won't (this release) | — |

## 7. Success Metrics

- Performing a transfer or an approved card purchase in the demo UI
  results in a corresponding row in `notification-orchestrator`'s feed
  within one polling interval (5 seconds), sourced from a completely
  separate service and database than the one that processed the request.
- Neither `internal-transfer-service` nor `card-authorization-service`
  takes on a hard runtime dependency on Kafka: every existing test for
  both services continues to pass unmodified in shape (same assertions on
  the transfer/authorization outcome), with the only change being new
  tests that publishing is *attempted*, never that it must succeed.
- Zero changes to `accounts-service`, `ledger-service`,
  `card-management-service`'s issuance/activation/blocking logic, or
  either publishing service's decision logic (the daily-limit check, the
  currency/active-account validation, etc.) — this release only adds a
  side effect after each decision is already made.

## 8. Milestones / Timeline

| Milestone | Target date | Owner |
| --- | --- | --- |
| ADR-0003 approved, event contracts + topic decided | 2026-09-02 | Platform Engineering |
| `TransferCompleted` publishing live in `internal-transfer-service` | 2026-09-02 | Platform Engineering |
| `CardAuthorizationApproved` publishing live in `card-authorization-service` | 2026-09-02 | Platform Engineering |
| `notification-orchestrator` consuming and recording both event types | 2026-09-02 | Platform Engineering |
| Kafka + notification-orchestrator wired into docker-compose/gateway/environments | 2026-09-02 | Platform Engineering |
| web-banking Activity feed | 2026-09-02 | Platform Engineering |
| Backlog items beyond this release (see `notifications-backlog.md`) | Unscheduled | Unassigned |

## 9. Dependencies

- `internal-transfer-service`, `card-authorization-service` — the two
  existing services this release adds a publishing side effect to,
  without changing their decision logic.
- `messaging/kafka` — the topic definition this release fills in
  (`transaction-events.yaml`), previously empty scaffolding.
- `docs/architecture/vertical-slice.md` — the pattern (service boundaries,
  environment profiles, docker-compose wiring, database-per-service) this
  release follows for the one new service it adds.

## 10. Out of Scope

- Everything listed under Non-Goals above.
- Every other topic scaffolded under `messaging/kafka/topics/` besides
  `transaction-events.yaml` (account-events, compliance-events,
  customer-events, fraud-events, notification-events, payment-events)
  remains empty — this release fills in exactly the one topic it needs.
- `services/notifications/notification-orchestrator` is the only service
  built under `services/notifications/`; any sibling services that domain
  eventually needs (e.g. a delivery-preferences service, a real email/SMS
  gateway integration) are unscoped, unscaffolded work.

## 11. Open Questions

- None outstanding at time of writing. Whether a future release needs
  per-producer topics instead of the shared `transaction-events` topic
  used here is addressed as a considered-and-deferred alternative in
  ADR-0003, not an open question blocking this release.
