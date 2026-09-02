# Functional Requirements Document — FinTech Platform Content Pack

**Version:** 1.0 · **Date:** 2026-08-24 · **Author:** OFFICE iQ Content Team · **Status:** Draft

## 1. Introduction

This FRD translates the FinTech Platform PRD's goals into concrete, testable content requirements for the workspace's catalog entry: the `company` record, the 30 daily `tickets`, the `dsaProblems` set, and the `quizQuestions` pool, as uploaded through the admin Catalog import/content endpoints.

## 2. Scope

Covers the shape and validation behavior of the four content artifacts produced for this workspace: company-tickets (single combined import), DSA problems, and quiz questions — matching the upload contract described in `templates/README.md` (ADR 0009, ADR 0027).

## 3. Functional Requirements

### FR-1: Company record import

- **Description:** The system must accept a `company` object establishing the FinTech Platform catalog entry with `businessDomain: BANKING_FINTECH` and a Java/React `category`.
- **Inputs:** `company.name`, `slug`, `businessDomain`, `baseRepoUrl` (required); `category`, `techStack`, `cultureProfile`, `branding` (optional).
- **Outputs:** A new catalog entry visible in Admin Catalog with the given slug.
- **Business rules:** `slug` must be lowercase/hyphenated and not already exist (409 if it does); `businessDomain` must be one of the three allowed enum values.
- **Acceptance criteria:** Import succeeds with a 2xx response and the workspace appears in the catalog with the correct business domain and tech stack.
- **Traces to:** PR-1, BR-1

### FR-2: 30-day ticket set

- **Description:** The system must accept exactly one ticket per simulation day (1–30), each traceable to a real file, service, or doc in the `fintech-platform` repo.
- **Inputs:** `tickets[]`, each with `day`, `title`, `description`, `type`, `priority`, `estimatedHours`, `acceptanceCriteria`.
- **Outputs:** 30 persisted ticket records, one per day, retrievable by the candidate in day order.
- **Business rules:** 1–30 tickets, no duplicate `day` values; `type` must be one of the nine allowed enum values; every ticket type in the enum should appear at least once across the set.
- **Acceptance criteria:** All 30 tickets upload with zero validation errors; each ticket's `description` names a specific service/file/doc from the codebase.
- **Traces to:** PR-1, PR-2, PR-3, BR-2, BR-3, BR-4

### FR-3: Ledger-safety constraint on tickets

- **Description:** No ticket in the set may direct a candidate to weaken or bypass the double-entry balanced-entry invariant in `general-ledger-service`.
- **Inputs:** The full `tickets[]` array.
- **Outputs:** A ticket set where every ledger-adjacent ticket (day 4, 21, 22, 27) only adds read paths, splits services, or adds test coverage — never removes or relaxes the balance check.
- **Business rules:** Manual content review checks this before upload; there is no server-side enforcement of ticket semantics.
- **Acceptance criteria:** Review of tickets touching `general-ledger-service` confirms no instruction to remove or weaken the JournalEntry balance constraint.
- **Traces to:** PR-2, BR-3

### FR-4: DSA problem set

- **Description:** The system must accept 30 DSA problems, one per simulation day, drawn from problem types genuinely representative of IBM technical interviews.
- **Inputs:** `dsaProblems[]`, each with `day`, `id`, `title`, `difficulty`, `topics`, `prompt`, `example`.
- **Outputs:** 30 persisted DSA problems matched to their simulation day at read time.
- **Business rules:** 1–30 items, `title` and `prompt` required, `difficulty` in {Easy, Medium, Hard}; the entry must already have tickets uploaded before DSA can be patched in.
- **Acceptance criteria:** All 30 problems upload with zero validation errors; difficulty distribution is weighted toward Easy/Medium with a small number of Hard problems, consistent with published IBM interview-question difficulty data.
- **Traces to:** PR-4, BR-5

### FR-5: Quiz question pool

- **Description:** The system must accept a quiz pool of multiple-choice questions blending general IBM-interview CS/Java fundamentals with FinTech Platform-specific architecture concepts.
- **Inputs:** `quizQuestions[]`, each with `question`, exactly 4 `options`, `correctIndex` (0–3), plus `id`, `topic`, `difficulty`, `explanation`.
- **Outputs:** A persisted quiz pool the candidate draws from during the simulation; `correctIndex`/`explanation` withheld from candidates until after submission.
- **Business rules:** 1–200 items; malformed individual questions are dropped rather than failing the whole upload; the entry must already have tickets uploaded before quiz can be patched in.
- **Acceptance criteria:** All questions upload without being silently dropped; each question's `correctIndex` matches its intended correct option on manual spot check.
- **Traces to:** PR-5, BR-6

## 4. Non-Functional Requirements

| Category | Requirement |
| --- | --- |
| Data integrity | The whole company+tickets bundle is normalized before any database write — an invalid file must produce a 422 with a full `details` array, never a partial write |
| Content accuracy | DSA and quiz content should be traceable to published, current interview-question sources at time of authoring |
| Consistency | Ticket/DSA/quiz content must stay consistent with the actual state of the `fintech-platform` repo (file paths, service names) it references |
| Auditability | Every ticket carries a stable `ticketId` so candidate progress can be tracked per ticket across a run |

## 5. Use Cases / User Flows

1. Content admin uploads `company-tickets.json` via **Add workspace** → the catalog entry and its 30 tickets are created in one call.
2. Content admin uploads `dsa-problems.json` via the same modal or the existing entry's **Upload DSA problems JSON** → 30 DSA problems are patched onto the active content pack.
3. Content admin uploads `quiz-questions.json` via **Upload Quiz JSON** → the quiz pool is patched onto the active content pack.
4. Candidate opens the workspace, reads day 1's ticket, and runs `docker compose up --build` to start the stack, then works through tickets in day order.
5. Candidate attempts that day's DSA problem and quiz questions alongside the ticket.

## 6. Data Requirements

- **Company:** name, slug, businessDomain, category, techStack, cultureProfile, baseRepoUrl, branding.
- **Ticket:** day, ticketId, title, description, type, priority, estimatedHours, acceptanceCriteria[].
- **DSA problem:** day, id, title, difficulty, topics[], prompt, example.
- **Quiz question:** id, topic, difficulty, question, options[4], correctIndex, explanation.

## 7. Interface Requirements

- `POST /api/admin/catalog/import` — company + tickets (+ optional DSA/quiz) combined import.
- `POST /catalog/:id/content/dsa` — patch DSA problems onto an existing entry.
- `POST /catalog/:id/content/quiz` — patch quiz questions onto an existing entry.
- `POST /api/admin/catalog/:id/documents/{brd|prd|frd}` — upload this BRD/PRD/FRD as PDF/DOCX once exported.

## 8. Assumptions & Dependencies

- Depends on the `fintech-platform` repository's README and `docs/architecture/vertical-slice.md` remaining the accurate description of what is and isn't built.
- Assumes the content-pack upload endpoints and their validation rules (ADR 0009, ADR 0027) are unchanged from what `templates/README.md` documents.

## 9. Traceability Matrix

| FRD ID | PRD ID | BRD ID |
| --- | --- | --- |
| FR-1 | PR-1 | BR-1 |
| FR-2 | PR-1 | BR-2, BR-4 |
| FR-3 | PR-2 | BR-3 |
| FR-4 | PR-4 | BR-5 |
| FR-5 | PR-5 | BR-6 |
