package com.cinavault.android.data

data class RemoteSession(
    val endpoint: String,
    val token: String,
    val email: String,
    val expiresAt: String,
    val permissions: List<String>,
)

data class MediaItem(
    val mediaKey: String,
    val title: String,
    val mediaType: String,
    val year: Int?,
    val rating: Double?,
    val overview: String?,
    val genre: String?,
    val duration: Long?,
    val fileSize: Long?,
    val resolution: String?,
    val codec: String?,
    val verified: Boolean,
    val watched: Boolean,
    val favorite: Boolean,
    val dateAdded: String,
    val lastPlayed: String?,
    val tmdbId: String?,
    val imdbId: String?,
    val artworkUrl: String?,
    val streamUrl: String,
)

data class ServerInfo(
    val name: String,
    val product: String,
    val version: String,
    val build: String,
    val displayName: String,
    val releaseTag: String,
    val accountEmail: String,
    val permissions: List<String>,
    val remoteTransport: String,
    val mediaIdentifiers: String,
    val localPathsExposed: Boolean,
)

data class ControlMetric(
    val label: String,
    val value: String,
    val status: String = "normal",
)

data class ControlAction(
    val id: String,
    val label: String,
    val description: String,
    val enabled: Boolean = true,
    val dangerous: Boolean = false,
)

data class ControlSection(
    val id: String,
    val title: String,
    val subtitle: String,
    val metrics: List<ControlMetric> = emptyList(),
    val actions: List<ControlAction> = emptyList(),
)

data class ControlSnapshot(
    val available: Boolean,
    val generatedAt: String,
    val message: String,
    val sections: Map<String, ControlSection>,
) {
    fun section(id: String): ControlSection? = sections[id]

    companion object {
        fun unavailable(message: String): ControlSnapshot = ControlSnapshot(
            available = false,
            generatedAt = "",
            message = message,
            sections = emptyMap(),
        )
    }
}

enum class AppDestination(val label: String, val parityId: String) {
    Library("Library", "library"),
    Sources("Media Sources", "sources"),
    Downloads("Downloads", "downloads"),
    LiveTv("Live TV", "live-tv"),
    Server("Server Core", "server"),
    Security("Security", "security"),
    Remote("Remote Access", "remote"),
    Advanced("Advanced", "advanced"),
    CloudNas("Cloud & NAS", "cloud-nas"),
    Extensions("Extensions", "extensions"),
    Intelligence("AI Autopilot", "ai-autopilot"),
    Settings("Settings", "settings"),
    Casting("Casting", "casting"),
    Player("Now Playing", "player"),
}

data class CinaVaultUiState(
    val session: RemoteSession? = null,
    val serverInfo: ServerInfo? = null,
    val library: List<MediaItem> = emptyList(),
    val controlSnapshot: ControlSnapshot = ControlSnapshot.unavailable(
        "Control services have not synchronized yet.",
    ),
    val selectedMedia: MediaItem? = null,
    val destination: AppDestination = AppDestination.Library,
    val searchQuery: String = "",
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val runningControlAction: String? = null,
    val statusMessage: String = "Ready",
    val errorMessage: String? = null,
    val autopilotEnabled: Boolean = true,
    val lastRefreshEpochMillis: Long? = null,
) {
    val filteredLibrary: List<MediaItem>
        get() {
            val query = searchQuery.trim().lowercase()
            if (query.isEmpty()) return library
            return library.filter { item ->
                listOfNotNull(
                    item.title,
                    item.mediaType,
                    item.year?.toString(),
                    item.genre,
                    item.resolution,
                    item.codec,
                ).any { value -> value.lowercase().contains(query) }
            }
        }
}
