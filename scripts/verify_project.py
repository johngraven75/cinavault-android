#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]

REQUIRED = {
    "app/src/main/AndroidManifest.xml": [
        'android:usesCleartextTraffic="false"',
        'android:networkSecurityConfig="@xml/network_security_config"',
        'CinaVaultCastOptionsProvider',
        'CinaVaultPlaybackService',
    ],
    "app/src/main/res/xml/network_security_config.xml": [
        'cleartextTrafficPermitted="false"',
    ],
    "app/src/main/java/com/cinavault/android/security/SecureSessionStore.kt": [
        "AndroidKeyStore",
        "AES/GCM/NoPadding",
        "setRandomizedEncryptionRequired(true)",
    ],
    "app/src/main/java/com/cinavault/android/network/CinaVaultApi.kt": [
        'require(url.protocol.equals("https"',
        '"/api/library"',
        '"/api/auth/password"',
        '"/api/auth/access-key"',
        'setRequestProperty("Authorization", "Bearer $value")',
    ],
    "app/src/main/java/com/cinavault/android/data/Models.kt": [
        "mediaKey",
        "artworkUrl",
        "streamUrl",
        "AI Autopilot",
    ],
    "app/src/main/java/com/cinavault/android/ui/CinaVaultApp.kt": [
        "ExperienceBackdrop",
        "SpatialCommandBar",
        "SpatialNavigationRail",
        "SpatialBottomNavigation",
        "AnimatedContent",
    ],
    "app/src/main/java/com/cinavault/android/ui/LibraryScreen.kt": [
        "GridCells.Adaptive",
        "RemoteArtwork",
        "AI-MANAGED LIBRARY",
    ],
    "app/src/main/java/com/cinavault/android/ui/PlayerScreen.kt": [
        "DefaultHttpDataSource.Factory",
        '"Authorization" to "Bearer $token"',
        "CastContext",
        "MediaLoadRequestData",
    ],
    "app/src/main/java/com/cinavault/android/CinaVaultViewModel.kt": [
        "SecureSessionStore",
        "smartSort",
        "runAutopilotNow",
    ],
    "app/build.gradle.kts": [
        'versionCode = 2',
        'versionName = "2.0.2"',
        'compileSdk = 36',
        'targetSdk = 36',
        'minSdk = 24',
        'play-services-cast-framework:22.3.1',
        'media3-exoplayer:1.10.1',
    ],
    "docs/CARRY_FORWARD.md": [
        "full-file",
        "Opaque remote media keys",
        "Google Cast",
        "AI Autopilot",
    ],
}

errors: list[str] = []
for relative_path, tokens in REQUIRED.items():
    path = ROOT / relative_path
    if not path.is_file():
        errors.append(f"missing file: {relative_path}")
        continue
    text = path.read_text(encoding="utf-8")
    for token in tokens:
        if token not in text:
            errors.append(f"missing token in {relative_path}: {token}")

models = (ROOT / "app/src/main/java/com/cinavault/android/data/Models.kt").read_text(encoding="utf-8")
remote_media_start = models.find("data class MediaItem")
remote_media_end = models.find("data class ServerInfo")
media_model = models[remote_media_start:remote_media_end]
for forbidden in ("filePath", "file_path", "localPath", "absolutePath"):
    if forbidden in media_model:
        errors.append(f"remote MediaItem exposes forbidden local path field: {forbidden}")

if errors:
    print("CinaVault Android carry-forward verification failed:")
    for error in errors:
        print(f" - {error}")
    sys.exit(1)

print(f"CinaVault Android carry-forward verification passed ({len(REQUIRED)} files checked).")
