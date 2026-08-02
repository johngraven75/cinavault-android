package com.cinavault.android.data

import kotlinx.serialization.Serializable

const val SHARED_CONTRACT_VERSION: Int = 1
const val METADATA_PROVIDER_FIXTURE_SHA256: String =
    "b7ca1f8748296ce7651d17dec3165ac8a37e3aca321eaf558199299b44b5820d"
const val ARTWORK_CACHE_FIXTURE_SHA256: String =
    "d9b08d61cd3451278315102da031d0834db315639d49d7c16efb533ddd26e697"

@Serializable
data class MetadataProviderContract(
    val id: String,
    val name: String,
    val category: String,
    val enabled: Boolean,
    val requiresKey: Boolean,
    val implemented: Boolean,
    val endpoint: String? = null,
    val customEndpoint: String? = null,
)

@Serializable
data class MetadataProviderRegistryContract(
    val schemaVersion: Int,
    val policy: String,
    val credentialsStorage: String,
    val portableAcrossOperatingSystems: Boolean,
    val providers: List<MetadataProviderContract>,
)

@Serializable
data class ArtworkCacheEntryContract(
    val schemaVersion: Int,
    val mediaKey: String,
    val kind: String,
    val mimeType: String,
    val byteLength: Long,
    val sha256: String,
    val width: Int,
    val height: Int,
    val sourceProvider: String,
    val cacheState: String,
    val deliveryPath: String,
    val localPathExposed: Boolean,
    val expiresAt: String? = null,
)

interface MetadataProviderRegistryInterface {
    fun metadataProviderContract(): MetadataProviderRegistryContract
}

interface ArtworkCacheInterface {
    fun artworkContract(): ArtworkCacheEntryContract
}

fun MetadataProviderRegistryContract.validateContract(): MetadataProviderRegistryContract {
    require(schemaVersion == SHARED_CONTRACT_VERSION) {
        "Unsupported metadata provider contract version: $schemaVersion"
    }
    require(policy == "all_providers_enabled") {
        "Metadata provider policy must enable all providers"
    }
    require(credentialsStorage == "native_secure_store") {
        "Credentials must remain in the native secure store"
    }
    require(portableAcrossOperatingSystems) {
        "Metadata provider registry must be cross-platform portable"
    }
    require(providers.isNotEmpty()) {
        "Metadata provider registry cannot be empty"
    }
    require(providers.all(MetadataProviderContract::enabled)) {
        "Every metadata provider must remain enabled"
    }
    require(providers.map(MetadataProviderContract::id).distinct().size == providers.size) {
        "Metadata provider identifiers must be unique"
    }
    return this
}

fun ArtworkCacheEntryContract.validateContract(): ArtworkCacheEntryContract {
    require(schemaVersion == SHARED_CONTRACT_VERSION) {
        "Unsupported artwork contract version: $schemaVersion"
    }
    require(mediaKey.isNotBlank()) { "Artwork media key is required" }
    require(kind in setOf("poster", "backdrop", "thumbnail")) {
        "Unsupported artwork kind"
    }
    require(mimeType.startsWith("image/")) {
        "Artwork MIME type must be an image"
    }
    require(byteLength in 1..(25L * 1024L * 1024L)) {
        "Artwork byte length is outside the supported range"
    }
    require(sha256.length == 64 && sha256.all(Char::isHexDigit)) {
        "Artwork SHA-256 must be a 64-character hexadecimal value"
    }
    require(width > 0 && height > 0) {
        "Artwork dimensions must be positive"
    }
    require(!localPathExposed) {
        "Artwork contracts must not expose local filesystem paths"
    }
    require(deliveryPath.startsWith("/api/artwork/")) {
        "Artwork delivery path must use the secured artwork API"
    }
    return this
}

private fun Char.isHexDigit(): Boolean =
    this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
