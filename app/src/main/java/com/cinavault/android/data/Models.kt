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
    val name: String = "CinaVault Premium",
    val product: String = "CinaVault Embedded Media Server",
    val version: String = "2.0.2",
    val build: String = "v2 Build 2",
    val accountEmail: String = "",
    val permissions: List<String> = emptyList(),
    val remoteTransport: String = "HTTPS relay",
    val mediaIdentifiers: String = "opaque media keys",
    val localPathsExposed: Boolean = false,
)

enum class AppDestination(val label: String) {
    Library("Library"),
    Player("Now Playing"),
    Remote("Remote"),
    Casting("Casting"),
    Intelligence("AI Autopilot"),
    Settings("Settings"),
}

data class CinaVaultUiState(
    val session: RemoteSession? = null,
    val serverInfo: ServerInfo? = null,
    val library: List<MediaItem> = emptyList(),
    val selectedMedia: MediaItem? = null,
    val destination: AppDestination = AppDestination.Library,
    val searchQuery: String = "",
    val loading: Boolean = false,
    val refreshing: Boolean = false,
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
