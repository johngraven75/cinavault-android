# CinaVault Android Carry-Forward Registry

Every release must retain these capabilities unless an explicit migration document and replacement tests are merged in the same pull request.

## v2 Build 2 foundation

- HTTPS-only CinaVault server endpoints.
- Android Keystore AES-GCM session encryption.
- Account-password and access-key login.
- Opaque remote media keys; no server file paths in client models.
- Authenticated library, artwork, and byte-range stream access.
- Media3 playback and background media session service.
- Google Cast automatic discovery, reconnection, and playback handoff.
- Spatial command shell with animated backdrop and adaptive phone/tablet navigation.
- Compact adaptive media-card library with poster and metadata display.
- Remote access telemetry and account permissions.
- AI Autopilot library synchronization, smart ordering, and repair insights.
- Stable media-key reconciliation after every refresh so enriched metadata and posters update the visible media card and current selection.
- HF token ownership remains in the Windows secure store; Android displays only server-reported readiness.
- Metadata provider readiness is synchronized from Windows at startup and after remote control actions.
- CI validation, signed/installable APK generation, checksums, guarded releases, maintenance, and dependency automation.

## Windows v2.1.7-beta-3 parity note (2026-11-02)

Version pairing only: Android numbering now follows Windows v2.1.7-beta-3
(main `7bd0b75c`). This does not claim feature duplication.

- Retained: HTTPS-only server endpoints, Android Keystore AES-GCM session
  storage, account-password and access-key login, opaque remote media keys,
  authenticated library/artwork/byte-range streaming, Media3 playback and
  Cast handoff, spatial dark UI, AI Autopilot remote actions, and the
  control-snapshot remote views.
- Carried over in this step: the shared parity contract reference, version
  pairing, governance checks, and a `WindowsBeta3Parity` status model with
  unit tests that name every beta-3 feature and its Android status.
- Explicitly left behind (flagged, not dropped): card Match/Unmatch/Lookup,
  local adult poster/metadata automation, vault encryption (passphrase,
  XChaCha20-Poly1305, Argon2id), WireGuard VPN, Google Drive OAuth/listing,
  and adult-provider gating / store-safe mode. Session storage must never
  be described as vault encryption. Follow-up port work or an explicit
  owner exception for that specific change is required before the platform
  set can be called green.

## Change policy

- full-file replacements only; do not submit partial patch hunks as the delivered implementation.
- No registered capability may be removed or weakened unless the repository owner explicitly requests its removal.
- Never remove a registered capability to make a build pass.
- Repair the implementation or its environment and rerun all gates.
- Do not publish an APK unless lint, unit tests, debug build, release build, artifact verification, signature verification, and checksum generation all succeed.
