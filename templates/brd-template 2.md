<!--
  OFFICE iQ — BRD DUMMY TEMPLATE (rules-only, no real content)

  Purpose: fill this in with a workspace's real Business Requirements
  Document, export it as PDF or Word (.docx), then upload it via the admin
  Catalog screen's edit-entry form → "Project documents (BRD / PRD / FRD)"
  section → BRD picker (POST /api/admin/catalog/:id/documents/brd, ADR 0024,
  docx support ADR 0025). Candidates download it from the workspace's About
  Project page ("Project documents" panel).

  Accepted formats: application/pdf or .docx
  (application/vnd.openxmlformats-officedocument.wordprocessingml.document).
  Size cap: WORKSPACE_DOC_MAX_MB (default 15MB, backend/.env.example).
  One BRD per workspace — re-uploading replaces the existing file, there is
  no version history (see ADR 0024's Decision, point 1).

  Replace every [bracketed] instruction below with real content, then delete
  this comment block before exporting. Section order/headings below are a
  recommendation, not a validated schema — unlike the JSON content templates
  in this folder, BRD/PRD/FRD files are opaque uploads (no server-side
  parsing), so nothing here is enforced at upload time beyond file type/size.
-->

# Business Requirements Document — [Workspace / Company Name]

**Version:** [1.0] · **Date:** [YYYY-MM-DD] · **Author:** [name/team] · **Status:** [Draft / Approved]

## 1. Executive Summary

[2–4 sentences: what business problem this project/workspace solves, who
asked for it, and the expected outcome. Written for a reader who has never
seen this workspace before — this is often the first thing a candidate
reads on the About Project page.]

## 2. Business Objectives

[Numbered list of the business goals this project serves — e.g. "Reduce
support ticket volume by 20%", "Launch a self-serve onboarding flow by
Q3". Each objective should be measurable, not aspirational fluff.]

1. [Objective 1]
2. [Objective 2]

## 3. Scope

### 3.1 In Scope
- [What this project/workspace covers]

### 3.2 Out of Scope
- [What it explicitly does not cover — as important as what's in scope]

## 4. Stakeholders

| Role | Name/Team | Interest |
| --- | --- | --- |
| [Sponsor] | [name] | [why they care] |
| [Product Owner] | [name] | [why they care] |

## 5. Business Requirements

[The core list — each requirement should be a single, testable business
need, not an implementation detail (that belongs in the FRD). Give each an
ID so the FRD/PRD can trace back to it.]

| ID | Requirement | Priority |
| --- | --- | --- |
| BR-1 | [e.g. "Users must be able to reset their password without contacting support"] | [Must / Should / Could] |
| BR-2 | [...] | [...] |

## 6. Assumptions & Constraints

- **Assumptions:** [things taken as given, e.g. "all users have a modern browser"]
- **Constraints:** [budget, timeline, tech, regulatory limits]

## 7. Success Metrics / KPIs

- [How success will be measured, tied back to Section 2's objectives]

## 8. Risks

| Risk | Impact | Mitigation |
| --- | --- | --- |
| [...] | [High/Med/Low] | [...] |

## 9. Glossary

- **[Term]** — [definition, for any domain jargon a new reader wouldn't know]

## 10. Approval

| Name | Role | Date |
| --- | --- | --- |
| [...] | [...] | [...] |
