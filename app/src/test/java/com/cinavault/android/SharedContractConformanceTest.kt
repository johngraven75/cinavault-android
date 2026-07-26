package com.cinavault.android

import com.cinavault.android.data.ARTWORK_CACHE_FIXTURE_SHA256
import com.cinavault.android.data.ArtworkCacheEntryContract
import com.cinavault.android.data.METADATA_PROVIDER_FIXTURE_SHA256
import com.cinavault.android.data.MetadataProviderRegistryContract
import com.cinavault.android.data.validateContract
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedContractConformanceTest {
    private val json = Json {
        encodeDefaults = true
        explicitNulls = true
        ignoreUnknownKeys = false
        isLenient = false
    }

    @Test
    fun metadataProviderGoldenFileHasCanonicalHashAndRoundTrips() {
        val text = readFixture("metadata-provider-registry.json")
        assertEquals(METADATA_PROVIDER_FIXTURE_SHA256, sha256(text))

        val decoded = json.decodeFromString<MetadataProviderRegistryContract>(text)
            .validateContract()
        val encoded = json.parseToJsonElement(json.encodeToString(decoded))
        val golden = json.parseToJsonElement(text)

        assertEquals(golden, encoded)
        assertEquals(listOf("tvmaze", "tmdb"), decoded.providers.map { it.id })
        assertTrue(decoded.providers.all { it.enabled })
    }

    @Test
    fun artworkGoldenFileHasCanonicalHashAndRoundTrips() {
        val text = readFixture("artwork-cache-entry.json")
        assertEquals(ARTWORK_CACHE_FIXTURE_SHA256, sha256(text))

        val decoded = json.decodeFromString<ArtworkCacheEntryContract>(text)
            .validateContract()
        val encoded = json.parseToJsonElement(json.encodeToString(decoded))
        val golden = json.parseToJsonElement(text)

        assertEquals(golden, encoded)
        assertFalse(decoded.localPathExposed)
        assertTrue(decoded.deliveryPath.startsWith("/api/artwork/"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun artworkContractRejectsLocalPathExposure() {
        val decoded = json.decodeFromString<ArtworkCacheEntryContract>(
            readFixture("artwork-cache-entry.json"),
        )
        decoded.copy(localPathExposed = true).validateContract()
    }

    private fun readFixture(name: String): String {
        val fixture = locateRepositoryRoot()
            .resolve("contracts")
            .resolve("v1")
            .resolve("golden")
            .resolve(name)
        return String(Files.readAllBytes(fixture), StandardCharsets.UTF_8)
    }

    private fun locateRepositoryRoot(): Path {
        var candidate: Path? = Paths.get(System.getProperty("user.dir")).toAbsolutePath()
        while (candidate != null) {
            if (Files.isDirectory(candidate.resolve("contracts").resolve("v1"))) {
                return candidate
            }
            candidate = candidate.parent
        }
        error("Could not locate contracts/v1 from ${System.getProperty("user.dir")}")
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
}
