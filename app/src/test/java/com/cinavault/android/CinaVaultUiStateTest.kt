package com.cinavault.android

import com.cinavault.android.data.CinaVaultUiState
import com.cinavault.android.data.MediaItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CinaVaultUiStateTest {
    @Test
    fun searchFiltersAcrossTitleGenreAndResolution() {
        val library = listOf(
            media("alpha", "Arrival", "Science Fiction", "4K"),
            media("beta", "Moonlight", "Drama", "1080p"),
        )

        assertEquals(
            listOf("Arrival"),
            CinaVaultUiState(library = library, searchQuery = "science").filteredLibrary.map { it.title },
        )
        assertEquals(
            listOf("Moonlight"),
            CinaVaultUiState(library = library, searchQuery = "1080").filteredLibrary.map { it.title },
        )
    }

    @Test
    fun remoteModelContainsOnlyOpaquePaths() {
        val item = media("opaque-key", "CinaVault", "Media", "4K")
        assertFalse(item.mediaKey.contains("/"))
        assertFalse(item.streamUrl.contains("C:\\"))
        assertFalse(item.streamUrl.contains("/Users/"))
    }

    private fun media(key: String, title: String, genre: String, resolution: String) =
        MediaItem(
            mediaKey = key,
            title = title,
            mediaType = "movie",
            year = 2026,
            rating = 8.0,
            overview = "overview",
            genre = genre,
            duration = 7_200,
            fileSize = 1_000,
            resolution = resolution,
            codec = "h265",
            verified = true,
            watched = false,
            favorite = false,
            dateAdded = "2026-07-25",
            lastPlayed = null,
            tmdbId = null,
            imdbId = null,
            artworkUrl = "/api/artwork/$key",
            streamUrl = "/api/stream/$key",
        )
}
