# CinaVault Android

Android client for CinaVault Premium **v2.13 Build 1.13**.

This repository follows the same release discipline as the Windows application:

- full-file replacements only; no partial patch hunks;
- no regressions or removal of existing user-facing capabilities;
- pull-request validation before merge;
- automated debug and release APK builds;
- signed release artifacts when signing secrets are configured;
- SHA-256 checksums for published artifacts;
- guarded release publication;
- automated dependency maintenance, stale-work hygiene, artifact cleanup, and failure reporting;
- Android parity with the v2 Build 2 spatial UI, account access, remote library, streaming, casting, and AI-managed media experience.

The Android application will connect to a CinaVault Premium server through its authenticated HTTPS remote endpoint. Local desktop-only server administration features will be represented by remote controls and status views rather than attempting to run the Windows server runtime on Android.

## Downloadable test build

The `Android v2.13 Build 1.13 Installable TEST APK` workflow publishes a clearly
labeled debug-signed prerelease APK, SHA-256 checksum, and install notes. This TEST
package uses the `.debug` application id so it cannot be confused with or replace
the production-signed app.
