# Build 171 — LumaSift Companion Design (Android)

## Purpose

**LumaSift** brings a cinematic, high-contrast duplicate-resolution command center to CinaVault Android. The owner selects videos, MP3 audio, DOCX documents, PDFs, and/or images before asking an authenticated Windows host to scan local drives, external volumes, and mounted NAS shares; inspect exact duplicate groups; understand the quality choice; and explicitly approve a quarantine-first resolution plan.

The Android product is a companion client in the existing CinaVault architecture. It deliberately delegates network-share traversal, complete content hashing, ranking, file movement, audit logging, and permanent deletion to the Windows host that owns the media paths.

## Architecture

### Front end

A responsive LumaSift destination is added to the Compose navigation shell. It preserves the existing spatial, high-contrast CinaVault presentation while introducing the prism brand mark, live percentage progress, active file/source, storage reclamation summary, quality-ranked duplicate cards, and disposition history. Every destructive-looking action uses explicit wording and requires confirmation before sending a quarantine-plan request.

### Connector / integration

`CinaVaultApi` adds typed, authenticated LumaSift endpoints for status, selected-category scan start, plan retrieval, and confirmed plan application. The session token is passed through the existing secure session flow. NAS credentials do not enter the Android client and no password is persisted in UI state, logs, or request history.

### Back end

The Android view model exposes a bounded polling lifecycle that activates only while the LumaSift screen is visible and a scan is active. It cancels in-flight work on lifecycle teardown, maps transport errors to user-safe states, and rejects stale or unapproved plans. No local arbitrary-file deletion or SMB implementation is added to the mobile client.

## Public Contracts

| Contract | Android behavior |
|---|---|
| `GET /api/lumasift/status` | Maps server phase, processed/total counts, percentage, current item, and source reports to reactive state. |
| `GET /api/lumasift/plan` | Shows a read-only score-explained resolution plan and file-level dispositions. |
| `POST /api/lumasift/plan/apply` | Sends a plan identifier only after an explicit owner confirmation for quarantine. |

## Safety and Validation

The plan action is disabled while scanning, while applying, if no plan exists, or when the server marks the plan stale. The confirmation dialog states that the proposed files move to quarantine, includes the item count and projected space recovery, and displays server results without masking partial failures. Rendering treats remote path values as plain text.

Validation includes API decoding and redaction tests, compose state tests for active/idle/error/stale/applying states, destructive confirmation tests, lifecycle cancellation tests, accessibility checks, and a manual authenticated flow against the Windows server contract.

## Brand System

The duplicate-resolution feature is branded **LumaSift**. Its three-facet prism/checkmark symbol sits on an obsidian background and uses electric cyan, ultraviolet violet, hot magenta, and a restrained gold highlight. CinaVault remains the connection and server identity.
