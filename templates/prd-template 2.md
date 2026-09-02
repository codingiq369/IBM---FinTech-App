<!--
  OFFICE iQ — PRD DUMMY TEMPLATE (rules-only, no real content)

  Purpose: fill this in with a workspace's real Product Requirements
  Document, export it as PDF or Word (.docx), then upload it via the admin
  Catalog screen's edit-entry form → "Project documents (BRD / PRD / FRD)"
  section → PRD picker (POST /api/admin/catalog/:id/documents/prd, ADR 0024,
  docx support ADR 0025). Candidates download it from the workspace's About
  Project page ("Project documents" panel).

  Accepted formats: application/pdf or .docx
  (application/vnd.openxmlformats-officedocument.wordprocessingml.document).
  Size cap: WORKSPACE_DOC_MAX_MB (default 15MB, backend/.env.example).
  One PRD per workspace — re-uploading replaces the existing file, there is
  no version history (see ADR 0024's Decision, point 1).

  Replace every [bracketed] instruction below with real content, then delete
  this comment block before exporting. Section order/headings below are a
  recommendation, not a validated schema — unlike the JSON content templates
  in this folder, BRD/PRD/FRD files are opaque uploads (no server-side
  parsing), so nothing here is enforced at upload time beyond file type/size.
-->

# Product Requirements Document — [Workspace / Product Name]

**Version:** [1.0] · **Date:** [YYYY-MM-DD] · **Author:** [name/team] · **Status:** [Draft / Approved]

## 1. Problem Statement

[What problem does this product solve, for whom, and why now? Should read
as the "why" that the BRD's business objectives feed into.]

## 2. Goals

- [Goal 1 — user- or product-facing, distinct from the BRD's business objectives]
- [Goal 2]

## 3. Non-Goals

- [What this product/release explicitly will not do — prevents scope creep]

## 4. Target Users / Personas

| Persona | Description | Primary need |
| --- | --- | --- |
| [e.g. "New hire, week 1"] | [...] | [...] |

## 5. User Stories

[As a [persona], I want to [action], so that [benefit]. Group by feature/epic.]

- As a [persona], I want to [...], so that [...].
- As a [persona], I want to [...], so that [...].

## 6. Features & Requirements

[MoSCoW-prioritized. Link each back to a BRD requirement ID where relevant.]

| ID | Feature | Description | Priority | Traces to (BRD) |
| --- | --- | --- | --- | --- |
| PR-1 | [...] | [...] | [Must / Should / Could / Won't] | [BR-1] |
| PR-2 | [...] | [...] | [...] | [...] |

## 7. Success Metrics

- [Quantifiable metric + target, e.g. "time-to-first-action < 2 minutes"]

## 8. Milestones / Timeline

| Milestone | Target date | Owner |
| --- | --- | --- |
| [...] | [...] | [...] |

## 9. Dependencies

- [Other teams, systems, or documents this PRD depends on]

## 10. Out of Scope

- [Explicitly excluded items, to avoid ambiguity for the reader]

## 11. Open Questions

- [Anything unresolved at time of writing — remove once answered]
