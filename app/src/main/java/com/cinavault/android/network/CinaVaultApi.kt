package com.cinavault.android.network

import com.cinavault.android.data.MediaItem
import com.cinavault.android.data.RemoteSession
import com.cinavault.android.data.ServerInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class CinaVaultApi {
    suspend fun loginWithPassword(
        endpoint: String,
        email: String,
        password: String,
    ): RemoteSession = withContext(Dispatchers.IO) {
        val normalizedEndpoint = normalizeEndpoint(endpoint)
        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
            .toString()
        val json = JSONObject(
            requestText(
                endpoint = normalizedEndpoint,
                path = "/api/auth/password",
                method = "POST",
                body = body,
                token = null,
            ),
        )
        parseSession(normalizedEndpoint, json)
    }

    suspend fun loginWithAccessKey(
        endpoint: String,
        accessKey: String,
    ): RemoteSession = withContext(Dispatchers.IO) {
        val normalizedEndpoint = normalizeEndpoint(endpoint)
        val body = JSONObject().put("accessKey", accessKey.trim()).toString()
        val json = JSONObject(
            requestText(
                endpoint = normalizedEndpoint,
                path = "/api/auth/access-key",
                method = "POST",
                body = body,
                token = null,
            ),
        )
        parseSession(normalizedEndpoint, json)
    }

    suspend fun loadServerInfo(session: RemoteSession): ServerInfo =
        withContext(Dispatchers.IO) {
            val json = JSONObject(
                requestText(
                    endpoint = session.endpoint,
                    path = "/api/server/info",
                    method = "GET",
                    body = null,
                    token = session.token,
                ),
            )
            ServerInfo(
                name = json.optString("name", "CinaVault Premium"),
                product = json.optString("product", "CinaVault Embedded Media Server"),
                version = json.optString("version", "2.0.2"),
                build = json.optString("build", "v2 Build 2"),
                accountEmail = json.optString("accountEmail", session.email),
                permissions = json.optJSONArray("permissions").stringList(),
                remoteTransport = json.optString("remoteTransport", "HTTPS relay"),
                mediaIdentifiers = json.optString("mediaIdentifiers", "opaque media keys"),
                localPathsExposed = json.optBoolean("localPathsExposed", false),
            )
        }

    suspend fun loadLibrary(session: RemoteSession): List<MediaItem> =
        withContext(Dispatchers.IO) {
            val array = JSONArray(
                requestText(
                    endpoint = session.endpoint,
                    path = "/api/library",
                    method = "GET",
                    body = null,
                    token = session.token,
                ),
            )
            buildList {
                for (index in 0 until array.length()) {
                    add(parseMediaItem(array.getJSONObject(index)))
                }
            }
        }

    suspend fun loadArtwork(
        session: RemoteSession,
        artworkPath: String,
    ): ByteArray = withContext(Dispatchers.IO) {
        requestBytes(
            endpoint = session.endpoint,
            path = artworkPath,
            token = session.token,
        )
    }

    fun absoluteUrl(session: RemoteSession, path: String): String =
        resolveUrl(session.endpoint, path)

    private fun parseSession(endpoint: String, json: JSONObject): RemoteSession {
        val token = json.optString("sessionToken").ifBlank {
            throw IllegalStateException("Server did not return a session token")
        }
        return RemoteSession(
            endpoint = endpoint,
            token = token,
            email = json.optString("email"),
            expiresAt = json.optString("expiresAt"),
            permissions = json.optJSONArray("permissions").stringList(),
        )
    }

    private fun parseMediaItem(json: JSONObject): MediaItem = MediaItem(
        mediaKey = json.optString("mediaKey"),
        title = json.optString("title", "Untitled"),
        mediaType = json.optString("mediaType", "video"),
        year = json.nullableInt("year"),
        rating = json.nullableDouble("rating"),
        overview = json.nullableString("overview"),
        genre = json.nullableString("genre"),
        duration = json.nullableLong("duration"),
        fileSize = json.nullableLong("fileSize"),
        resolution = json.nullableString("resolution"),
        codec = json.nullableString("codec"),
        verified = json.optBoolean("verified", false),
        watched = json.optBoolean("watched", false),
        favorite = json.optBoolean("favorite", false),
        dateAdded = json.optString("dateAdded"),
        lastPlayed = json.nullableString("lastPlayed"),
        tmdbId = json.nullableString("tmdbId"),
        imdbId = json.nullableString("imdbId"),
        artworkUrl = json.nullableString("artworkUrl"),
        streamUrl = json.optString("streamUrl"),
    )

    private fun requestText(
        endpoint: String,
        path: String,
        method: String,
        body: String?,
        token: String?,
    ): String {
        val connection = openConnection(endpoint, path, method, token)
        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.use { stream ->
                stream.write(body.toByteArray(Charsets.UTF_8))
            }
        }
        return connection.readResponse().toString(Charsets.UTF_8)
    }

    private fun requestBytes(
        endpoint: String,
        path: String,
        token: String,
    ): ByteArray {
        val connection = openConnection(endpoint, path, "GET", token)
        return connection.readResponse()
    }

    private fun openConnection(
        endpoint: String,
        path: String,
        method: String,
        token: String?,
    ): HttpsURLConnection {
        val url = URL(resolveUrl(endpoint, path))
        require(url.protocol.equals("https", ignoreCase = true)) {
            "CinaVault Android only accepts HTTPS server endpoints"
        }
        return (url.openConnection() as HttpsURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 45_000
            useCaches = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Cache-Control", "no-store")
            token?.takeIf(String::isNotBlank)?.let { value ->
                setRequestProperty("Authorization", "Bearer $value")
            }
        }
    }

    private fun HttpsURLConnection.readResponse(): ByteArray {
        val status = responseCode
        val source = if (status in 200..299) inputStream else errorStream
        val output = ByteArrayOutputStream()
        source?.use { input -> input.copyTo(output) }
        val bytes = output.toByteArray()
        if (status !in 200..299) {
            val message = bytes.toString(Charsets.UTF_8).ifBlank { responseMessage }
            throw IllegalStateException("CinaVault server returned HTTP $status: $message")
        }
        return bytes
    }

    private fun normalizeEndpoint(endpoint: String): String {
        val trimmed = endpoint.trim().trimEnd('/')
        val uri = URI(trimmed)
        require(uri.scheme.equals("https", ignoreCase = true)) {
            "Use the encrypted HTTPS CinaVault relay URL"
        }
        require(!uri.host.isNullOrBlank()) { "Enter a valid CinaVault server URL" }
        return trimmed
    }

    private fun resolveUrl(endpoint: String, path: String): String {
        if (path.startsWith("https://")) return path
        return endpoint.trimEnd('/') + "/" + path.trimStart('/')
    }

    private fun JSONArray?.stringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optString(index).takeIf(String::isNotBlank)?.let(::add)
            }
        }
    }

    private fun JSONObject.nullableString(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf(String::isNotBlank)

    private fun JSONObject.nullableInt(key: String): Int? =
        if (isNull(key)) null else optInt(key)

    private fun JSONObject.nullableLong(key: String): Long? =
        if (isNull(key)) null else optLong(key)

    private fun JSONObject.nullableDouble(key: String): Double? =
        if (isNull(key)) null else optDouble(key)
}
