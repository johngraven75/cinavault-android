# CinaVault Android — Build 171 Release Notes

## LumaSift Companion

Build 171 adds the **LumaSift** duplicate-resolution destination to the responsive Android shell. The interface uses the new prism mark and high-contrast cinematic palette to present an owner-controlled selection menu for **videos, MP3 audio, DOCX documents, PDFs, and images**, live percentage progress, active media name, exact duplicate groups, score evidence, recoverable storage, and explicit per-file dispositions.

From an authenticated HTTPS CinaVault session, Android can request a review-only LumaSift scan, refresh host status, inspect the path-redacted resolution plan, and approve a quarantine-first action. The Windows host remains the authority for scanning selected media folders, external volumes, authenticated NAS shares, hashing, scoring, and moving files.

## Safety and Compatibility

Android neither receives NAS credentials nor performs arbitrary local or remote file deletion. The explicit confirmation sends only an approved plan identifier; the Windows host revalidates the content hash and moves the lower-ranked exact duplicate to host-side quarantine. Existing CinaVault navigation, playback, remote access, and control surfaces are preserved. Restoring from quarantine remains the rollback path until the owner explicitly empties it on the host.
