#!/usr/bin/env python3
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def read(path: str) -> str:
    target = ROOT / path
    if not target.is_file():
        errors.append(f"missing file: {path}")
        return ""
    return target.read_text(encoding="utf-8").replace("\r\n", "\n")


def require(path: str, *tokens: str) -> str:
    text = read(path)
    for token in tokens:
        if token not in text:
            errors.append(f"missing invariant in {path}: {token}")
    return text


gradle = require(
    "app/build.gradle.kts",
    "versionCode = 112",
    'versionName = "2.0.12"',
    'buildConfigField("String", "CINAVAULT_BUILD", "\\\"v2.12 Build 1.12\\\"")',
    "compileSdk = 36",
    "targetSdk = 36",
    "minSdk = 24",
    "play-services-cast-framework:22.3.1",
    "media3-exoplayer:1.10.1",
)

require(
    "app/src/main/AndroidManifest.xml",
    'android:usesCleartextTraffic="false"',
    'android:networkSecurityConfig="@xml/network_security_config"',
    'android:allowBackup="false"',
    "CinaVaultCastOptionsProvider",
    "CinaVaultPlaybackService",
)
require("app/src/main/res/xml/network_security_config.xml", 'cleartextTrafficPermitted="false"')
require(
    "app/src/main/java/com/cinavault/android/security/SecureSessionStore.kt",
    "AndroidKeyStore",
    "AES/GCM/NoPadding",
    "setRandomizedEncryptionRequired(true)",
)
require(
    "app/src/main/java/com/cinavault/android/network/CinaVaultApi.kt",
    'require(url.protocol.equals("https"',
    '"/api/library"',
    '"/api/auth/password"',
    '"/api/auth/access-key"',
    '"/api/control/snapshot"',
    '"/api/control/action"',
    "HttpsURLConnection",
    "instanceFollowRedirects = false",
)
require(
    "app/src/main/java/com/cinavault/android/ui/LibraryScreen.kt",
    "GridCells.Adaptive",
    "RemoteArtwork",
    "AI-MANAGED LIBRARY",
)
require(
    "app/src/main/java/com/cinavault/android/ui/PlayerScreen.kt",
    "DefaultHttpDataSource.Factory",
    "CastContext",
    "MediaLoadRequestData",
)
require(
    "app/src/main/java/com/cinavault/android/CinaVaultViewModel.kt",
    "SecureSessionStore",
    "smartSort",
    "runAutopilotNow",
    "loadControlSnapshot",
    "runControlAction",
)

contract_text = read("docs/platform-parity.json")
if contract_text:
    try:
        contract = json.loads(contract_text)
    except json.JSONDecodeError as exc:
        errors.append(f"invalid docs/platform-parity.json: {exc}")
    else:
        expected_repositories = [
            "johngraven75/CinaVault-Premium",
            "johngraven75/cinavault-android",
            "johngraven75/Cinavault-Server-Premium-Edition-iOS",
        ]
        if contract.get("includedRepositories") != expected_repositories:
            errors.append("platform contract included repository set drifted")
        reference = contract.get("reference", {})
        if reference.get("repository") != "johngraven75/CinaVault-Premium":
            errors.append("Android parity contract must reference CinaVault-Premium")
        if reference.get("release") not in {"v2-build-1.12", "v2.12", "v2.0.12"}:
            errors.append("Android parity contract must reference Windows v2.12 Build 1.12")
        policy = contract.get("changePolicy", {})
        for key in ("fullFileReplacementsOnly", "noRegressions", "crossPlatformAuditRequired"):
            if policy.get(key) is not True:
                errors.append(f"platform policy must keep {key}=true")

release = require(
    ".github/workflows/release.yml",
    "ANDROID_KEYSTORE_BASE64",
    "ANDROID_KEYSTORE_PASSWORD",
    "ANDROID_KEY_ALIAS",
    "ANDROID_KEY_PASSWORD",
    "Require stable release signing identity",
    "apksigner",
)
if "keytool -genkeypair" in release or "ephemeral-automation" in release:
    errors.append("release workflow must not generate an ephemeral signing identity")

if errors:
    print("CinaVault Android v2.12 Build 1.12 parity verification failed:")
    for error in errors:
        print(f" - {error}")
    sys.exit(1)

print("CinaVault Android v2.12 Build 1.12 parity verification passed.")
