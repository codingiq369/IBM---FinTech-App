# Business Requirements Document — FinTech Platform

**Version:** 1.0 · **Date:** 2026-08-24 · **Author:** Platform Engineering · **Status:** Draft

## 1. Executive Summary

FinTech Platform is a reference digital-banking codebase used as a hands-on simulation workspace: a candidate joins as if onto a real platform team building core banking infrastructure. The repository scaffolds a full bank (customer, accounts, cards, lending, trading, fraud, ML, data, security) but currently ships one fully working vertical slice — onboarding a customer, opening an account, and transferring money between accounts, with every balance backed by a real double-entry general ledger. The business need is a realistic, safely-scoped environment where engineers can be evaluated on and trained in the skills real banking-platform work demands: service boundaries, financial correctness, and safe extension of an existing system, without the cost or risk of a production banking environment.

## 2. Business Objectives

1. Provide a technically credible banking-domain workspace that reflects how a real digital bank is actually built (microservices, database-per-service, an auditable ledger), not a toy CRUD app.
2. Give candidates/engineers a structured, day-by-day body of work (tickets) that mirrors real platform priorities — fix defects, hit security and compliance points (KYC, PCI-DSS), harden reliability, and extend the product (cards, lending, events) — over a 30-day simulation.
3. Pair the hands-on ticket work with a DSA practice track and a knowledge-check quiz pool so technical readiness is assessed alongside real system work.

## 3. Scope

### 3.1 In Scope
- The five implemented services (customer, account, general-ledger, internal-transfer, api-gateway) and the web-banking demo UI, as the base the 30-day ticket program builds on.
- 30 daily tickets spanning bug fixes, feature work, security/compliance hardening, performance, reliability, incident response, tech-debt, and deployment — extending the base slice into cards, lending, event messaging, and authentication.
- A 30-question DSA practice set aligned to problem types commonly asked in IBM technical interviews.
- A quiz question pool covering both general software-engineering fundamentals commonly asked at IBM and domain-specific concepts from this codebase (double-entry bookkeeping, idempotency, database-per-service, circuit breakers).

### 3.2 Out of Scope
- Building out the untouched scaffold areas not reached by the 30-day ticket set (trading, fraud, ML platform, full Kubernetes/Terraform infrastructure, multi-currency FX).
- Any connection to real banking rails, real customer data, or real payment networks — this is a simulated environment only.
- Production-grade authentication/authorization beyond the single hardening ticket included in the 30-day set (day 23).

## 4. Stakeholders

| Role | Name/Team | Interest |
| --- | --- | --- |
| Sponsor | Platform Engineering Leadership | Wants a repeatable, realistic way to assess and train engineers on banking-platform skills |
| Product Owner | OFFICE iQ Content Team | Owns the workspace catalog entry and its content pack (tickets, DSA, quiz) |
| Candidates / Engineers | Simulation participants | Complete the 30-day ticket program, DSA problems, and quizzes |

## 5. Business Requirements

| ID | Requirement | Priority |
| --- | --- | --- |
| BR-1 | The workspace must present a working, runnable banking system on day 1 (`docker compose up --build`), not just documentation | Must |
| BR-2 | Every simulation day (1–30) must have exactly one ticket, each traceable to a real file/service in the codebase | Must |
| BR-3 | The ledger's double-entry invariant (debits equal credits) must never be weakened by any ticket in the program | Must |
| BR-4 | The program must include at least one ticket each for security, incident response, and compliance-adjacent work (KYC, PCI-DSS) | Should |
| BR-5 | DSA practice content must reflect problem types genuinely representative of IBM technical interviews | Should |
| BR-6 | Quiz content must cover both general CS/Java fundamentals asked in IBM interviews and this codebase's specific architecture | Should |

## 6. Assumptions & Constraints

- **Assumptions:** Participants have Docker installed and can run a multi-service local stack; participants have baseline Java/Spring Boot and React/TypeScript familiarity going in.
- **Constraints:** The backend Java services were authored without Maven Central access and were not compiled before handoff (see README) — day 2's ticket exists specifically to address this. Content-pack hard limits apply: at most 30 tickets (one per day), at most 30 DSA problems, at most 200 quiz questions.

## 7. Success Metrics / KPIs

- 100% of the 30 simulation days have a ticket that references a real, existing part of the codebase.
- All 30 DSA problems and all quiz questions pass content-pack validation on upload with zero `422` errors.
- Candidates completing the 30-day program can articulate the ledger's double-entry invariant and at least one reliability pattern (idempotency, retries/circuit breaker, outbox) introduced during the program.

## 8. Risks

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Backend services fail to compile locally (no Maven Central access at authoring time) | High | Day 2 ticket dedicates time to fixing build issues before other work begins |
| Candidates skip foundational days and jump to extension tickets (cards/lending), missing core ledger concepts | Medium | Tickets are ordered by day and each extension ticket depends on concepts introduced earlier in the sequence |
| DSA/quiz content drifts from what IBM interviews actually ask over time | Low | Content is grounded in current published interview-question compilations and should be refreshed periodically |

## 9. Glossary

- **Vertical slice** — the one fully-built, end-to-end path (onboarding → account → transfer) through an otherwise scaffolded system.
- **Double-entry bookkeeping** — an accounting method where every transaction records equal, offsetting debit and credit entries.
- **KYC** — Know Your Customer; identity verification required before a financial account can be opened.
- **Idempotency key** — a client-supplied token letting a server safely recognize and dedupe a retried request.

## 10. Approval

| Name | Role | Date |
| --- | --- | --- |
| Platform Engineering Leadership | Sponsor | 2026-08-24 |
| OFFICE iQ Content Team | Product Owner | 2026-08-24 |
