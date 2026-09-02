# Backlog — Event-Driven Notifications

Engineering ticket backlog for the increment described in
[`prd-event-driven-notifications.md`](prd-event-driven-notifications.md)
and [`frd-event-driven-notifications.md`](frd-event-driven-notifications.md).
Same plain engineering-ticket format as `card-issuance-backlog.md`, for the
same reason (`templates/company-tickets-template.json` is a different,
non-reusable schema — see that file's own note).

## Epic: Publishing (internal-transfer-service, card-authorization-service)

| ID | Story | Priority | Points | Status | Acceptance criteria |
| --- | --- | --- | --- | --- | --- |
| NOTIF-1 | Publish `TransferCompleted` after a transfer commits COMPLETED | Must | 3 | **Done** | `TransferServiceTest` |
| NOTIF-2 | Publish `CardAuthorizationApproved` after a purchase commits APPROVED | Must | 3 | **Done** | `CardAuthorizationServiceTest` |
| NOTIF-3 | Publishing never blocks or fails the originating request | Must | 3 | **Done** | `TransferEventPublisherTest`, `CardAuthorizationEventPublisherTest` |
| NOTIF-4 | `TransferFailed` / `CardAuthorizationDeclined` events | Won't (this release) | 3 | Backlog | No consumer needs them yet; PRD PR-8 |
| NOTIF-5 | Transactional outbox for guaranteed-delivery publishing | Could | 8 | Backlog | Closes the narrow commit-then-crash window named in ADR-0003 |

## Epic: Consumption (notification-orchestrator)

| ID | Story | Priority | Points | Status | Acceptance criteria |
| --- | --- | --- | --- | --- | --- |
| NOTIF-6 | Consume `transaction-events`, parse both event types | Must | 3 | **Done** | `TransactionEventParserTest` |
| NOTIF-7 | Record each event as an audit-trail row | Must | 2 | **Done** | `NotificationServiceTest` |
| NOTIF-8 | Duplicate-safe recording (unique constraint on eventId) | Must | 2 | **Done** | `NotificationServiceTest`; `V1__create_notification_records_table.sql` |
| NOTIF-9 | `GET /api/notifications` (recent feed) | Must | 1 | **Done** | `NotificationController` |
| NOTIF-10 | Dead-letter handling for messages that fail to parse | Could | 3 | To Do | Currently logged and dropped, per ADR-0003 |
| NOTIF-11 | Full idempotent-consumption design (beyond a unique constraint) | Could | 5 | Backlog | Needed once a consumer has a real external side effect (see NOTIF-13) |

## Epic: Platform wiring

| ID | Story | Priority | Points | Status | Acceptance criteria |
| --- | --- | --- | --- | --- | --- |
| NOTIF-12 | Single-node KRaft Kafka broker in docker-compose | Must | 3 | **Done** | `deployment/docker/docker-compose.yml` |
| NOTIF-13 | `transaction-events` topic definition | Must | 1 | **Done** | `messaging/kafka/topics/transaction-events.yaml` |
| NOTIF-14 | Route `/api/notifications/**` through api-gateway | Must | 1 | **Done** | `gateways/api-gateway/src/main/resources/application.yml` |
| NOTIF-15 | Wire notification-orchestrator + Kafka into all four environment `.env` files | Must | 1 | **Done** | `environments/*/.env` |
| NOTIF-16 | Activity page in web-banking | Should | 3 | **Done** | `apps/web-banking/src/pages/notifications/NotificationsPage.tsx`; `npm run build`/`lint`/`test` all pass |
| NOTIF-17 | Multi-broker Kafka with real replication | Should | 8 | To Do | See `docs/operations/deployment.md` for what a production topology needs |
| NOTIF-18 | Kubernetes overlays / Terraform for notification-orchestrator + Kafka | Should | 5 | To Do | Extend `infrastructure/kubernetes` and `infrastructure/terraform` the way `docs/operations/deployment.md` describes for the existing services |
| NOTIF-19 | CI pipeline coverage for notification-orchestrator | Should | 3 | To Do | Mirror whatever pipeline definition covers the existing services today |

## Epic: Beyond this release (deferred by the PRD's Non-Goals)

| ID | Story | Priority | Points | Status | Notes |
| --- | --- | --- | --- | --- | --- |
| NOTIF-20 | Real notification delivery — email | Won't (this release) | 8 | Backlog | Needs an email provider integration; see `docs/domains/notifications.md` |
| NOTIF-21 | Real notification delivery — SMS / push | Won't (this release) | 8 | Backlog | Needs a delivery-channel provider and per-customer preferences |
| NOTIF-22 | Per-customer notification preferences / opt-out | Won't (this release) | 5 | Backlog | Needs `notification-orchestrator` to know which customer an event is for — see FR-6's note that today's feed is platform-wide |
| NOTIF-23 | Consumers for the other six scaffolded topics (account, compliance, customer, fraud, payment) | Won't (this release) | 21 | Backlog | Each is its own future increment; this release fills in only `transaction-events` |
| NOTIF-24 | Schema registry / compatibility enforcement across producers and consumers | Won't (this release) | 8 | Backlog | See ADR-0003's "Alternatives considered" |

**Status legend:** *Done* = merged and tested in this increment. *To Do* =
scoped, not started. *Backlog* = intentionally deferred, not yet scoped in
detail. *Won't (this release)* = explicitly out of scope per the PRD.
