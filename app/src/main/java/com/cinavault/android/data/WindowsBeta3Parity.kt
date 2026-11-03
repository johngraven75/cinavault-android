package com.cinavault.android.data

/**
 * Honest status of the Android remote client against Windows
 * CinaVault-Premium v2.1.7-beta-3 (main 7bd0b75c).
 *
 * This is scaffolding for parity work, not a claim of duplication:
 * a feature marked NOT_IMPLEMENTED is genuinely absent on Android,
 * VIA_SERVER means the Windows server does the work and Android only
 * displays or triggers it through the authenticated remote API, and
 * PLATFORM_LIMITATION means the Windows capability is represented by
 * an equivalent remote control instead of running locally on Android.
 *
 * Standing rule (2026-11-02): every Windows change carries over to ALL
 * OS versions on every new build. Android being green on its own does
 * NOT make the platform set green; the set is green only when every
 * platform is checked and passing through publish and release.
 */
enum class AndroidParityStatus {
    IMPLEMENTED,
    VIA_SERVER,
    NOT_IMPLEMENTED,
    PLATFORM_LIMITATION,
}

data class WindowsBeta3Feature(
    val id: String,
    val windowsFeature: String,
    val androidStatus: AndroidParityStatus,
    val note: String,
)

object WindowsBeta3Parity {
    const val REFERENCE_RELEASE: String = "v2.1.7-beta-3"
    const val REFERENCE_MAIN: String = "7bd0b75c"
    const val REFERENCE_REPOSITORY: String = "johngraven75/CinaVault-Premium"

    const val STANDING_RULE: String =
        "Every Windows change carries over to ALL OS versions on every new " +
            "build, nothing left behind unless the owner specifically says " +
            "otherwise for that change. Nothing counts as green until ALL OS " +
            "versions are greenlit through publish and release."

    val FEATURES: List<WindowsBeta3Feature> = listOf(
        WindowsBeta3Feature(
            id = "library-remote-browsing",
            windowsFeature = "Library browsing with authenticated artwork and streaming",
            androidStatus = AndroidParityStatus.VIA_SERVER,
            note = "Android browses the server library over HTTPS with opaque " +
                "media keys and authenticated artwork and byte-range streams; " +
                "it does not own the library database.",
        ),
        WindowsBeta3Feature(
            id = "player-and-casting",
            windowsFeature = "Media playback and casting",
            androidStatus = AndroidParityStatus.IMPLEMENTED,
            note = "Media3 ExoPlayer playback with a background media session " +
                "service and Google Cast discovery and playback handoff.",
        ),
        WindowsBeta3Feature(
            id = "metadata-and-poster-refresh",
            windowsFeature = "Metadata and poster refresh on media cards",
            androidStatus = AndroidParityStatus.VIA_SERVER,
            note = "Enrichment runs on the Windows server; Android refreshes " +
                "visible cards through stable media-key reconciliation. " +
                "There is no local enrichment engine on Android.",
        ),
        WindowsBeta3Feature(
            id = "card-match-unmatch-lookup",
            windowsFeature = "3-dot card menu: Match, Unmatch, Lookup",
            androidStatus = AndroidParityStatus.NOT_IMPLEMENTED,
            note = "LEFT BEHIND: no card dropdown menu exists in the Android " +
                "codebase, so Match, Unmatch and Lookup cannot be run from " +
                "an Android media card. Windows offers them in CardCornerMenu.",
        ),
        WindowsBeta3Feature(
            id = "background-ai-enrichment",
            windowsFeature = "Background AI enrichment and AI Autopilot",
            androidStatus = AndroidParityStatus.VIA_SERVER,
            note = "Enrichment and autopilot run on the server; Android shows " +
                "the agent panel and runs a server action only when the " +
                "control snapshot reports it as enabled.",
        ),
        WindowsBeta3Feature(
            id = "adult-poster-metadata-automation",
            windowsFeature = "Adult poster and metadata automation",
            androidStatus = AndroidParityStatus.NOT_IMPLEMENTED,
            note = "LEFT BEHIND: adult provider asset files are bundled but " +
                "no Android code references them to run local automation; " +
                "any automation today is server-side only.",
        ),
        WindowsBeta3Feature(
            id = "vault-encryption",
            windowsFeature = "App vault encryption: passphrase, XChaCha20-Poly1305, Argon2id",
            androidStatus = AndroidParityStatus.NOT_IMPLEMENTED,
            note = "LEFT BEHIND: no passphrase gate, XChaCha20-Poly1305 or " +
                "Argon2id exists on Android. SecureSessionStore is " +
                "AndroidKeyStore AES-GCM session storage only and is not " +
                "a vault; it must not be described as vault encryption.",
        ),
        WindowsBeta3Feature(
            id = "wireguard-vpn",
            windowsFeature = "WireGuard VPN install and profiles",
            androidStatus = AndroidParityStatus.NOT_IMPLEMENTED,
            note = "LEFT BEHIND: no WireGuard code, profile store or VPN " +
                "service exists on Android.",
        ),
        WindowsBeta3Feature(
            id = "google-drive",
            windowsFeature = "Google Drive OAuth and listing",
            androidStatus = AndroidParityStatus.NOT_IMPLEMENTED,
            note = "LEFT BEHIND: no Google Drive OAuth or listing code " +
                "exists on Android. On Windows, Drive listing and " +
                "ingestion remain a follow-up even after the OAuth work.",
        ),
        WindowsBeta3Feature(
            id = "adult-gating-store-safe",
            windowsFeature = "Adult-provider gating and store-safe mode",
            androidStatus = AndroidParityStatus.NOT_IMPLEMENTED,
            note = "LEFT BEHIND: Android has no store-safe edition gate. " +
                "Its shared contract requires every metadata provider to " +
                "remain enabled, and bundled adult provider assets are " +
                "marked enabled with no gate found in Android code.",
        ),
        WindowsBeta3Feature(
            id = "spatial-dark-ui-themes",
            windowsFeature = "Spatial dark UI with accent themes",
            androidStatus = AndroidParityStatus.IMPLEMENTED,
            note = "Dark CinaVault theme with electric-cyan, orchid, " +
                "magenta, emerald and solar accents, adaptive rail or " +
                "bottom navigation, and reduced-motion support.",
        ),
        WindowsBeta3Feature(
            id = "server-core-local",
            windowsFeature = "Local server core runtime",
            androidStatus = AndroidParityStatus.PLATFORM_LIMITATION,
            note = "Android is a remote client by design and does not run " +
                "the Windows server runtime. Server administration is " +
                "represented by authenticated control snapshot and action " +
                "views, which show an action only when the server " +
                "confirms support.",
        ),
        WindowsBeta3Feature(
            id = "other-destinations-remote-controls",
            windowsFeature = "Downloads, Live TV, Security, Cloud and NAS, Extensions, Settings destinations",
            androidStatus = AndroidParityStatus.VIA_SERVER,
            note = "These destinations exist as generic server control " +
                "screens on Android, not as local implementations of the " +
                "Windows tabs.",
        ),
    )

    /** Windows changes in this reference that Android has not yet ported. */
    fun leftBehind(): List<WindowsBeta3Feature> =
        FEATURES.filter { it.androidStatus == AndroidParityStatus.NOT_IMPLEMENTED }

    /**
     * The platform set is green only when every platform is green.
     * An Android-only green result must never be reported as set-green.
     */
    fun isPlatformSetGreen(allPlatformsGreen: Boolean): Boolean = allPlatformsGreen
}
