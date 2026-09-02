# Product Requirements Document — FinTech Platform Workspace

**Version:** 1.0 · **Date:** 2026-08-24 · **Author:** OFFICE iQ Content Team · **Status:** Draft

## 1. Problem Statement

Engineers preparing for or being evaluated against banking/fintech platform roles need a realistic environment to practice in — one with real service boundaries, a real financial correctness invariant, and real trade-offs — not a synthetic to-do-list app. FinTech Platform solves this by giving candidates a working microservices banking slice on day one, then a 30-day sequence of realistic tickets that mirror how such a platform is actually extended and hardened, backed by a matching DSA and quiz content pack for skills practice and assessment.

## 2. Goals

- Let a candidate get the full vertical slice (customer → account → ledger → transfer) running locally within the first session.
- Walk the candidate through fixing real defects, then hardening, then extending the system, in an order that builds understanding incrementally.
- Assess both applied engineering (tickets) and interview readiness (DSA, quiz) in one workspace.

## 3. Non-Goals

- This release does not implement real payment-rail integrations, real KYC/credit-bureau providers, or production-grade authentication beyond the single OAuth2/JWT hardening ticket.
- This release does not attempt to build out every scaffolded domain (trading, fraud, ML platform) — only cards and lending are extended, as representative examples.
- Grading/scoring rubrics for tickets are not defined here — this PRD covers workspace content only.

## 4. Target Users / Personas

| Persona | Description | Primary need |
| --- | --- | --- |
| Simulation candidate | An engineer being evaluated on or practicing banking-platform skills | A realistic, runnable system and clear daily objectives |
| Content admin | Uploads/maintains the workspace's tickets, DSA, and quiz content | Content that passes validation and stays traceable to the real codebase |
| Hiring/eval reviewer | Reviews a candidate's ticket completions | Tickets with clear, checkable acceptance criteria |

## 5. User Stories

- As a candidate, I want to run the whole system with one command, so that I can start working on day 1 without environment setup friction.
- As a candidate, I want each day's ticket to reference real files and services, so that my work reflects how the actual codebase is organized.
- As a candidate, I want DSA problems representative of real IBM interview questions, so that my practice time transfers to actual interviews.
- As a content admin, I want the tickets/DSA/quiz files to satisfy the content-pack's required fields and hard limits, so that upload succeeds without a 422.
- As a reviewer, I want measurable acceptance criteria on every ticket, so that I can objectively check completion.

## 6. Features & Requirements

| ID | Feature | Description | Priority | Traces to (BRD) |
| --- | --- | --- | --- | --- |
| PR-1 | 30-day ticket program | One ticket per simulation day, covering onboarding through capstone, spanning all nine ticket types (onboarding, feature, bug, security, performance, reliability, tech-debt, incident, deployment) | Must | BR-2, BR-4 |
| PR-2 | Ledger-safe extension path | No ticket may alter general-ledger-service's balanced-entry invariant; ledger-touching tickets (day 4, 21, 22, 27) only add read paths, split services, or add test coverage | Must | BR-3 |
| PR-3 | Security/compliance tickets | Dedicated tickets for KYC enforcement (day 3), PAN masking/PCI-DSS (day 14), idempotency incident response (day 18), and gateway authentication (day 23) | Should | BR-4 |
| PR-4 | DSA practice set | 30 problems, one per day, spanning Easy/Medium/Hard and the topic areas most represented in IBM's published interview-question data (arrays, strings, hash tables, DP, trees, graphs, design) | Should | BR-5 |
| PR-5 | Quiz question pool | MCQ pool blending general IBM-interview CS/Java fundamentals with codebase-specific concepts (double-entry bookkeeping, database-per-service, circuit breaker) | Should | BR-6 |

## 7. Success Metrics

- Candidate can reach a working local environment (BR-1) in under 15 minutes on a machine with Docker already installed.
- 100% of tickets, DSA problems, and quiz questions pass the content-pack's server-side validation on first upload attempt.
- Ticket acceptance criteria are specific enough that two independent reviewers reach the same pass/fail judgment on a sample of completed tickets.

## 8. Milestones / Timeline

| Milestone | Target date | Owner |
| --- | --- | --- |
| Ticket set (30 days) drafted and reviewed | 2026-08-24 | OFFICE iQ Content Team |
| DSA problem set drafted and reviewed | 2026-08-24 | OFFICE iQ Content Team |
| Quiz pool drafted and reviewed | 2026-08-24 | OFFICE iQ Content Team |
| Content pack uploaded to catalog | TBD | Admin Catalog owner |

## 9. Dependencies

- The underlying `fintech-platform` codebase (README and `docs/architecture/vertical-slice.md`) as the source of truth for what tickets can plausibly ask for.
- The OFFICE iQ content-pack upload endpoints and their validation rules (ADR 0009, ADR 0027) for tickets/DSA/quiz.

## 10. Out of Scope

- Any change to the underlying `fintech-platform` codebase itself as part of this content-pack authoring effort — the tickets describe work a candidate would do, they are not pre-applied to the repo.
- Localization of ticket/quiz/DSA content into languages other than English.

## 11. Open Questions

- Should the ticket program be forked into difficulty tracks (e.g. a shorter 10-day track) for candidates with less time?
- Should DSA problems be periodically re-validated against updated IBM interview-question data as new compilations are published?
