#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]

errors: list[str] = []


def read(relative_path: str) -> str:
    path = ROOT / relative_path
    if not path.is_file():
        errors.append(f"missing file: {relative_path}")
        return ""
    return path.read_text(encoding="utf-8").replace("\r\n", "\n")


def require_all(relative_path: str, tokens: list[str]) -> str:
    text = read(relative_path)
    for token in tokens:
        if token not in text:
            errors.append(f"missing invariant in {relative_path}: {token}")
    return text


def require_any(relative_path: str, alternatives: list[str], label: str) -> None:
    text = read(relative_path)
    if text and not any(token in text for token in alternatives):
        errors.append(f"missing invariant in {relative_path}: {label}")


manifest = require_all(
    "app/src/main/AndroidManifest.xml",
    [
        'android:usesCleartextTraffic="false"',
        'android:networkSecurityConfig="@xml/network_security_config"',
        "CinaVaultCastOptionsProvider",
        "CinaVaultPlaybackService",
    ],
)
require_all(
    "app/src/main/res/xml/network_security_config.xml",
    ['cleartextTrafficPermitted="false"'],
)
require_all(
    "app/src/main/java/com/cinavault/android/security/SecureSessionStore.kt",
    ["AndroidKeyStore", "AES/GCM/NoPadding", "setRandomizedEncryptionRequired(true)"],
)
api = require_all(
    "app/src/main/java/com/cinavault/android/network/CinaVaultApi.kt",
    [
        'require(url.protocol.equals("https"',
        '"/api/library"',
        '"/api/auth/password"',
        '"/api/auth/access-key"',
        "HttpsURLConnection",
    ],
)
if not re.search(r'setRequestProperty\(\s*"Authorization"\s*,\s*"Bearer \$[A-Za-z]+"\s*\)', api):
    errors.append("CinaVaultApi must attach a Bearer authorization header")

models = require_all(
    "app/src/main/java/com/cinavault/android/data/Models.kt",
    ["mediaKey", "artworkUrl", "streamUrl", "AI Autopilot"],
)
require_all(
    "app/src/main/java/com/cinavault/android/ui/CinaVaultApp.kt",
    [
        "ExperienceBackdrop",
        "SpatialCommandBar",
        "SpatialNavigationRail",
        "SpatialBottomNavigation",
        "AnimatedContent",
    ],
)
require_all(
    "app/src/main/java/com/cinavault/android/ui/LibraryScreen.kt",
    ["GridCells.Adaptive", "RemoteArtwork", "AI-MANAGED LIBRARY"],
)
player = require_all(
    "app/src/main/java/com/cinavault/android/ui/PlayerScreen.kt",
    ["DefaultHttpDataSource.Factory", "CastContext", "MediaLoadRequestData"],
)
if not re.search(r'"Authorization"\s+to\s+"Bearer \$token"', player):
    errors.append("PlayerScreen must attach the session Bearer token to media requests")
require_all(
    "app/src/main/java/com/cinavault/android/CinaVaultViewModel.kt",
    ["SecureSessionStore", "smartSort", "runAutopilotNow"],
)
require_all(
    "app/build.gradle.kts",
    [
        "versionCode = 2",
        'versionName = "2.0.2"',
        "compileSdk = 36",
        "targetSdk = 36",
        "minSdk = 24",
        "play-services-cast-framework:22.3.1",
        "media3-exoplayer:1.10.1",
    ],
)
require_all(
    "docs/CARRY_FORWARD.md",
    ["Opaque remote media keys", "Google Cast", "AI Autopilot"],
)
require_any(
    "docs/CARRY_FORWARD.md",
    ["full-file", "full file", "complete-file", "complete file"],
    "full-file replacement rule",
)

remote_media_start = models.find("data class MediaItem")
remote_media_end = models.find("data class ServerInfo")
if remote_media_start < 0 or remote_media_end <= remote_media_start:
    errors.append("Models.kt must define MediaItem before ServerInfo")
else:
    media_model = models[remote_media_start:remote_media_end]
    for forbidden in ("filePath", "file_path", "localPath", "absolutePath"):
        if forbidden in media_model:
            errors.append(f"remote MediaItem exposes forbidden local path field: {forbidden}")

if "android:allowBackup=\"false\"" not in manifest:
    errors.append("Android backups must remain disabled for encrypted session material")

if errors:
    print("CinaVault Android carry-forward verification failed:")
    for error in errors:
        print(f" - {error}")
    sys.exit(1)

print("CinaVault Android carry-forward verification passed.")
