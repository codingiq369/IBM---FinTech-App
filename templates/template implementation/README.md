# FinTech Platform — filled-in template implementation

These files are the `templates/` dummy templates filled in with real content for
the **FinTech Platform** workspace (the codebase in the sibling `fintech-platform/`
folder — a digital-banking microservices scaffold with one working vertical
slice: onboarding → open account → transfer, backed by a real double-entry
ledger).

| File | Fills in | Upload as |
| --- | --- | --- |
| `company-tickets.json` | `company-tickets-template.json` | **Add workspace** → "Company & tickets file (.json) *" (`POST /api/admin/catalog/import`) |
| `dsa-problems.json` | `dsa-template.json` | Same modal's "DSA problems file", or an existing entry's **Upload DSA problems JSON** |
| `quiz-questions.json` | `quiz-template.json` | Same modal's "Quiz file", or an existing entry's **Upload Quiz JSON** |
| `brd.pdf` (source: `brd.md`) | `brd-template.md` | Existing entry → Project documents → BRD picker |
| `prd.pdf` (source: `prd.md`) | `prd-template.md` | Existing entry → Project documents → PRD picker |
| `frd.pdf` (source: `frd.md`) | `frd-template.md` | Existing entry → Project documents → FRD picker |

The `.pdf` files are ready to upload as-is (`application/pdf`, well under the
default 15MB `WORKSPACE_DOC_MAX_MB` cap). The matching `.md` files are kept
alongside them as the editable source — regenerate the PDF after any edit
with, e.g., `pandoc brd.md -o brd.html --standalone --css pdf_style.css
--embed-resources && wkhtmltopdf brd.html brd.pdf`.

## What's inside

- **`company-tickets.json`** — a `BANKING_FINTECH` catalog entry ("FinTech
  Platform", Java + React) with 30 tickets, one per simulation day. Days 1–11
  fix and harden the existing vertical slice (build failures, KYC bypass,
  currency-validation bug, an outbox for crash safety, an idempotency
  incident). Days 12–20 extend it (a new `card-service`, a new
  `lending-service`, a Kafka `TransferCompleted` event, a notification
  consumer). Days 21–30 do harder platform work (splitting the ledger into
  its named sub-services, adding gateway auth, a login page, and a capstone
  CSV statement export). Every ticket names the real service/file it touches
  in `fintech-platform/`, and all nine ticket types (onboarding, feature,
  bug, security, performance, reliability, tech-debt, incident, deployment)
  are represented at least once.

- **`dsa-problems.json`** — 30 problems, one per day, chosen from patterns
  that recur across published IBM technical-interview question compilations
  (LeetCode/GeeksforGeeks/company-tagged lists): a run of foundational Easy
  problems (Two Sum, Valid Parentheses, linked-list basics), then Medium
  problems covering the topic areas IBM interviews weight most heavily —
  arrays, strings, hash tables, sliding window, stacks, trees, graphs, DP —
  and finishing with two Hard problems (Median of Two Sorted Arrays,
  Trapping Rain Water). See the Sources section below for what this was
  grounded in.

- **`quiz-questions.json`** — 40 multiple-choice questions blending (a)
  general technical topics IBM interviews are reported to ask — OOP
  fundamentals, Java concurrency (volatile, Callable vs Runnable, thread
  safety), design patterns (Singleton, Observer, Saga), DBMS/OS basics
  (ACID, indexing, deadlocks, semaphores, virtual memory), and SDLC/STLC —
  with (b) questions specific to this codebase (double-entry bookkeeping,
  database-per-service, idempotency, circuit breakers).

- **`brd.md` / `prd.md` / `frd.md`** — the business, product, and functional
  requirements for this workspace's *content pack itself* (not the
  fintech-platform codebase's own product requirements): why the 30-day
  ticket program is shaped the way it is, what it must guarantee (e.g. never
  weakening the ledger's balance invariant), and how the three JSON files
  and these documents trace back to those requirements.

## Before uploading

1. **Tickets:** review `company.baseRepoUrl` and `company.slug` in
   `company-tickets.json` and point them at your actual repository/slug —
   the placeholder values here are illustrative.
2. **Docs:** `brd.md`, `prd.md`, and `frd.md` need to be exported to PDF or
   `.docx` before upload — the server never parses the Markdown source
   directly (see `templates/README.md`).
3. **JSON validity:** all three JSON files were checked with `python3 -m
   json.tool` and respect the template's hard limits (≤30 tickets, ≤30 DSA
   problems, ≤200 quiz questions, exactly 4 options per quiz question,
   `correctIndex` in 0–3, no duplicate `day` values 1–30).

## Sources used for the DSA and quiz research

- [Top 50+ IBM Coding Interview Questions and Answers (2026) — Internshala](https://internshala.com/blog/ibm-coding-interview-questions/)
- [IBM Coding Questions with Answers 2026 — PrepInsta](https://prepinsta.com/ibm/coding/)
- [IBM LeetCode Interview Questions (115 problems) — CodeJeet](https://codejeet.com/company/ibm)
- [IBM LeetCode & Coding Interview Questions — Interview Solver](https://interviewsolver.com/interview-questions/ibm)
- [IBM Interview Questions and Answers for Technical Profiles — GeeksforGeeks](https://www.geeksforgeeks.org/interview-experiences/ibm-interview-questions-and-answers-for-technical-profiles/)
- [IBM Interview Questions with Real Candidate Experiences — PlacementPreparation.io](https://www.placementpreparation.io/blog/ibm-interview-questions-and-experience/)
