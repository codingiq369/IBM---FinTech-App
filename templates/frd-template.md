<!--
  OFFICE iQ — FRD DUMMY TEMPLATE (rules-only, no real content)

  Purpose: fill this in with a workspace's real Functional Requirements
  Document, export it as PDF or Word (.docx), then upload it via the admin
  Catalog screen's edit-entry form → "Project documents (BRD / PRD / FRD)"
  section → FRD picker (POST /api/admin/catalog/:id/documents/frd, ADR 0024,
  docx support ADR 0025). Candidates download it from the workspace's About
  Project page ("Project documents" panel).

  Accepted formats: application/pdf or .docx
  (application/vnd.openxmlformats-officedocument.wordprocessingml.document).
  Size cap: WORKSPACE_DOC_MAX_MB (default 15MB, backend/.env.example).
  One FRD per workspace — re-uploading replaces the existing file, there is
  no version history (see ADR 0024's Decision, point 1).

  Replace every [bracketed] instruction below with real content, then delete
  this comment block before exporting. Section order/headings below are a
  recommendation, not a validated schema — unlike the JSON content templates
  in this folder, BRD/PRD/FRD files are opaque uploads (no server-side
  parsing), so nothing here is enforced at upload time beyond file type/size.
-->

# Functional Requirements Document — [Workspace / Product Name]

**Version:** [1.0] · **Date:** [YYYY-MM-DD] · **Author:** [name/team] · **Status:** [Draft / Approved]

## 1. Introduction

[Purpose of this document and how it relates to the BRD/PRD for the same
workspace — the FRD is where business/product intent becomes concrete,
testable system behavior.]

## 2. Scope

[What system/module(s) this FRD covers.]

## 3. Functional Requirements

[The core of the document — one entry per discrete piece of system
behavior. Each should be specific enough that a developer could implement
it and a QA engineer could test it without guessing. Trace each back to a
PRD/BRD ID.]

### FR-1: [Requirement title]

- **Description:** [what the system must do]
- **Inputs:** [what triggers/feeds this behavior]
- **Outputs:** [expected result]
- **Business rules:** [validation, edge cases, constraints]
- **Acceptance criteria:** [bullet list of pass/fail conditions]
- **Traces to:** [PR-1 / BR-1]

### FR-2: [Requirement title]

- **Description:** [...]
- **Inputs:** [...]
- **Outputs:** [...]
- **Business rules:** [...]
- **Acceptance criteria:** [...]
- **Traces to:** [...]

## 4. Non-Functional Requirements

| Category | Requirement |
| --- | --- |
| Performance | [e.g. "page loads in < 2s on 3G"] |
| Security | [e.g. "all endpoints require auth"] |
| Availability | [e.g. "99.9% uptime"] |
| Accessibility | [e.g. "WCAG 2.1 AA"] |

## 5. Use Cases / User Flows

[Step-by-step flows for key scenarios — can be a numbered list or a
diagram reference.]

1. [User does X] → [system responds Y] → [...]

## 6. Data Requirements

- [Entities, fields, relationships the system must persist/expose]

## 7. Interface Requirements

- [APIs, UI screens, or integrations this functionality touches]

## 8. Assumptions & Dependencies

- [...]

## 9. Traceability Matrix

| FRD ID | PRD ID | BRD ID |
| --- | --- | --- |
| FR-1 | PR-1 | BR-1 |
| FR-2 | [...] | [...] |
