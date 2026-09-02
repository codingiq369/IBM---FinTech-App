# Domain: Notifications

## What's built

One service, real and runnable —
[`notification-orchestrator`](../../services/notifications/notification-orchestrator)
— consuming Kafka's `transaction-events` topic and recording each event it
understands as an audit-trail row a "recent activity" feed can read back.
See
[`docs/architecture/event-driven-architecture.md`](../architecture/event-driven-architecture.md)
for the full mechanics and
[ADR-0003](../architecture/architecture-decisions/ADR-0003-event-bus.md)
for why it's shaped this way, and the product documents for why this
exists at all:
[PRD](../product/prd-event-driven-notifications.md) ·
[FRD](../product/frd-event-driven-notifications.md).

| Service | Owns |
| --- | --- |
| [`notification-orchestrator`](../../services/notifications/notification-orchestrator) | Consuming `transaction-events`, recording what it sees, serving it back over `GET /api/notifications`. |

## What "notify" means at this stage — and what it doesn't yet

This is the one thing worth being precise about in this domain, because
the service's own name invites the wrong assumption. **Nothing in this
release sends a customer anything.** No email, no SMS, no push
notification, no in-app toast triggered by a background event — the
`web-banking` Activity page has to be open and polling to show anything at
all. "Notify," today, means exactly: *make a completed transaction visible
somewhere a person or another service can read it back*, which is a
necessary precursor to real notification delivery but is not delivery
itself.

The gap between "recorded a row" and "delivered a notification" is not an
oversight — it's why `notification_records`'s duplicate protection is
scoped the way it is (see the architecture doc's "Delivery guarantees"
section). A duplicate row is harmless; a duplicate email to a customer
saying "your transfer completed" twice is not. Building real delivery on
top of this foundation means revisiting that guarantee first
(`NOTIF-11` in `docs/product/notifications-backlog.md`), not just adding
an email client call to `NotificationService.recordEvent`.

## What's still scaffolding

Everything else under `services/notifications/` remains an empty
placeholder, and no sibling services for this domain have been named or
scaffolded yet the way `services/cards/` names `card-dispute-service`,
`card-rewards-service`, etc. — this domain has exactly one service so far.
Likely next additions, tracked as backlog items rather than open
questions:

| Would-be service or capability | Owns | Tracked as |
| --- | --- | --- |
| Real delivery (email) | Actually sending a customer an email when a notification-worthy event arrives | `docs/product/notifications-backlog.md`, NOTIF-20 |
| Real delivery (SMS / push) | Same, for other channels | NOTIF-21 |
| Delivery preferences | Which channel(s) a customer wants, and letting them opt out | NOTIF-22 — needs `notification-orchestrator` to know *which customer* an event is for, which it does not today (see below) |

## Key design decisions

- **The feed is platform-wide, not per-customer.** `notification-orchestrator`
  only knows what `TransferCompletedEvent` / `CardAuthorizationApprovedEvent`
  tell it — account and card ids, not customer ids — so
  `GET /api/notifications` returns everyone's recent activity, not "your"
  activity. Scoping this to a customer would mean either the event payload
  growing a customer id (a decision for whichever publishing service gets
  there first) or `notification-orchestrator` calling out to
  `accounts-service`/`card-management-service` to resolve one — deferred
  along with delivery preferences (NOTIF-22).
- **Only successful outcomes are notified about.** No `TransferFailed` or
  `CardAuthorizationDeclined` event exists yet (see the PRD's Non-Goals) —
  a failed transfer or declined purchase is already a fully-recorded API
  response in its owning service; nothing consumes a "this failed" signal
  yet, so none is published. Revisit the moment something does (fraud
  scoring wanting to see declines is the PRD's own example).
- **A decline is invisible here by design**, the same way a `DECLINED`
  card authorization is a normal, successful API response in
  `card-authorization-service` rather than an error — this domain simply
  has nothing to say about outcomes nobody asked it to track yet.

## On duplicate delivery

Kafka's at-least-once delivery means `notification-orchestrator` can see
the same event twice. Today that's harmless — a unique constraint on
`event_id` means a redelivery produces zero rows, not a duplicate one (see
the architecture doc). The moment this domain gains an actual delivery
channel, "harmless" stops being true by default, and whoever builds that
should re-read `docs/architecture/architecture-decisions/ADR-0003-event-bus.md`'s
dedup section before assuming the existing constraint is enough.
