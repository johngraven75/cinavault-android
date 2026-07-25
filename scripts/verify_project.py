#!/usr/bin/env python3
from pathlib import Path
import json
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
        'android:allowBackup="false"',
        "CinaVaultCastOptionsProvider",
        "CinaVaultPlaybackService",
    ],
)
require_all(
    "app/src/main/res/xml/network_security_config.xml",
    ['cleartextTrafficPermitted="false"'],
)
require_all(
    "app/src/main/res/values/themes.xml",
    [
        "android:windowBackground",
        "android:windowLightStatusBar">false" if False else "android:windowLightStatusBar",
        "android:windowLightNavigationBar",
    ],
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
        '"/api/control/snapshot"',
        '"/api/control/action"',
        "HttpsURLConnection",
        "instanceFollowRedirects = false",
        "Credentials must not be embedded in the server URL",
        "Unencrypted media URLs are not accepted",
    ],
)
if not re.search(r'setRequestProperty\(\s*"Authorization"\s*,\s*"Bearer \$[A-Za-z]+"\s*\)', api):
    errors.append("CinaVaultApi must attach a Bearer authorization header")

models = require_all(
    "app/src/main/java/com/cinavault/android/data/Models.kt",
    [
        "mediaKey",
        "artworkUrl",
        "streamUrl",
        "ControlSnapshot",
        "ControlSection",
        "AI Autopilot",
    ],
)
for destination in (
    'Library("Library", "library")',
    'Sources("Media Sources", "sources")',
    'Downloads("Downloads", "downloads")',
    'LiveTv("Live TV", "live-tv")',
    'Server("Server Core", "server")',
    'Security("Security", "security")',
    'Remote("Remote Access", "remote")',
    'Advanced("Advanced", "advanced")',
    'CloudNas("Cloud & NAS", "cloud-nas")',
    'Extensions("Extensions", "extensions")',
    'Intelligence("AI Autopilot", "ai-autopilot")',
    'Settings("Settings", "settings")',
):
    if destination not in models:
        errors.append(f"missing Windows destination parity in Models.kt: {destination}")

shell = require_all(
    "app/src/main/java/com/cinavault/android/ui/CinaVaultApp.kt",
    [
        "ExperienceBackdrop",
        "SpatialCommandBar",
        "SpatialNavigationRail",
        "SpatialBottomNavigation",
        "CommandPaletteOverlay",
        "PlatformControlScreen",
        "Ctrl/Command+K",
        "Key.K",
        "Color(0xFA02040D)",
        "destination-transition-safe",
        "slideInHorizontally",
        "slideOutHorizontally",
    ],
)
for forbidden in ("scaleIn(", "scaleOut(", "blur(", "RenderEffect"):
    if forbidden in shell:
        errors.append(f"Android navigation reintroduces compositor-risk token: {forbidden}")

require_all(
    "app/src/main/java/com/cinavault/android/ui/PlatformControlScreen.kt",
    [
        "CONTROL ENDPOINT PENDING",
        "no action is shown as available",
        "Source Constellation",
        "Incoming Media",
        "Live Signal",
        "Server Nexus",
        "Security Matrix",
        "Control Lab",
        "Cloud Mesh",
        "Extension Forge",
    ],
)
require_all(
    "app/src/main/java/com/cinavault/android/ui/CinaVaultRecoveryHost.kt",
    [
        "ApplicationExitInfo",
        "REASON_CRASH",
        "REASON_CRASH_NATIVE",
        "REASON_ANR",
        "Return to Library",
        "settings, and library records were preserved",
        "Color(0xFF02040A)",
    ],
)
require_all(
    "app/src/main/java/com/cinavault/android/MainActivity.kt",
    ["detectPreviousAbnormalExit", "CinaVaultRecoveryHost", "AppDestination.Library"],
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
    [
        "SecureSessionStore",
        "smartSort",
        "runAutopilotNow",
        "loadControlSnapshot",
        "runControlAction",
        "ControlSnapshot.unavailable",
    ],
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

contract_text = read("docs/platform-parity.json")
if contract_text:
    try:
        contract = json.loads(contract_text)
    except json.JSONDecodeError as error:
        errors.append(f"invalid docs/platform-parity.json: {error}")
    else:
        expected_repositories = [
            "johngraven75/CinaVault-Premium",
            "johngraven75/cinavault-android",
            "johngraven75/Cinavault-Server-Premium-Edition-iOS",
        ]
        if contract.get("includedRepositories") != expected_repositories:
            errors.append("platform contract included repository set drifted")
        excluded = contract.get("excludedRepositories", [])
        if len(excluded) != 1 or excluded[0].get("repository") != "johngraven75/Cinavault-Reimagined":
            errors.append("Cinavault-Reimagined must remain explicitly excluded")
        destination_ids = {entry.get("id") for entry in contract.get("destinations", [])}
        required_destinations = {
            "library", "sources", "downloads", "live-tv", "server", "security",
            "remote", "advanced", "cloud-nas", "extensions", "ai-autopilot", "settings",
        }
        if not required_destinations.issubset(destination_ids):
            errors.append("platform contract is missing required Windows destinations")
        defect_ids = {entry.get("id") for entry in contract.get("defectParity", [])}
        if defect_ids != {f"CVP-{index:03d}" for index in range(1, 10)}:
            errors.append("platform contract must track CVP-001 through CVP-009")
        policy = contract.get("changePolicy", {})
        for key in ("fullFileReplacementsOnly", "noRegressions", "crossPlatformAuditRequired"):
            if policy.get(key) is not True:
                errors.append(f"platform policy must keep {key}=true")

remote_media_start = models.find("data class MediaItem")
remote_media_end = models.find("data class ServerInfo")
if remote_media_start < 0 or remote_media_end <= remote_media_start:
    errors.append("Models.kt must define MediaItem before ServerInfo")
else:
    media_model = models[remote_media_start:remote_media_end]
    for forbidden in ("filePath", "file_path", "localPath", "absolutePath"):
        if forbidden in media_model:
            errors.append(f"remote MediaItem exposes forbidden local path field: {forbidden}")

if errors:
    print("CinaVault Android end-to-end parity verification failed:")
    for error in errors:
        print(f" - {error}")
    sys.exit(1)

print("CinaVault Android end-to-end parity verification passed.")
