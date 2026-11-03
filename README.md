# CinaVault Android

Android client for CinaVault Premium **v2.1.7-beta-3** (Windows main `7bd0b75c`).

> Scope, stated plainly: Android is a Kotlin native **remote client** for a CinaVault Premium server, not the Windows server itself. This PR pairs Android's version numbering with Windows v2.1.7-beta-3 and records the beta-3 gap audit in `docs/platform-parity.json` (`windowsBeta3`) and `app/src/main/java/com/cinavault/android/data/WindowsBeta3Parity.kt`. It does **not** make Android a full duplicate of Windows yet. Six Windows changes remain explicitly flagged as left behind on Android — card Match/Unmatch/Lookup, local adult poster/metadata automation, vault encryption, WireGuard VPN, Google Drive OAuth/listing, and adult-provider gating / store-safe mode — and require follow-up port work or an explicit owner exception for that change. Under the 2026-11-02 standing rule, Android alone being green does not make the platform set green: the set is green only when every OS version is greenlit through publish and release.

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

The `Android v2.1.7-beta-3 Installable TEST APK` workflow publishes a clearly
labeled debug-signed prerelease APK, SHA-256 checksum, and install notes. This TEST
package uses the `.debug` application id so it cannot be confused with or replace
the production-signed app.
