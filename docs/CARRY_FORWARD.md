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
- CI validation, signed/installable APK generation, checksums, guarded releases, maintenance, and dependency automation.

## Change policy

- Full-file replacements only; do not submit partial patch hunks as the delivered implementation.
- Never remove a registered capability to make a build pass.
- Repair the implementation or its environment and rerun all gates.
- Do not publish an APK unless lint, unit tests, debug build, release build, artifact verification, signature verification, and checksum generation all succeed.
