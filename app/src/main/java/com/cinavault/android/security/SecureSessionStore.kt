package com.cinavault.android.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.cinavault.android.data.RemoteSession
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureSessionStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun save(session: RemoteSession) {
        preferences.edit()
            .putString(KEY_ENDPOINT, encrypt(session.endpoint))
            .putString(KEY_TOKEN, encrypt(session.token))
            .putString(KEY_EMAIL, encrypt(session.email))
            .putString(KEY_EXPIRES_AT, encrypt(session.expiresAt))
            .putString(KEY_PERMISSIONS, encrypt(session.permissions.joinToString("\u001F")))
            .apply()
    }

    fun load(): RemoteSession? {
        val endpoint = decrypt(preferences.getString(KEY_ENDPOINT, null)) ?: return null
        val token = decrypt(preferences.getString(KEY_TOKEN, null)) ?: return null
        val email = decrypt(preferences.getString(KEY_EMAIL, null)) ?: return null
        val expiresAt = decrypt(preferences.getString(KEY_EXPIRES_AT, null)).orEmpty()
        val permissions = decrypt(preferences.getString(KEY_PERMISSIONS, null))
            ?.split("\u001F")
            ?.filter(String::isNotBlank)
            .orEmpty()
        return RemoteSession(endpoint, token, email, expiresAt, permissions)
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val payload = cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return runCatching {
            val payload = Base64.decode(value, Base64.NO_WRAP)
            require(payload.size > IV_SIZE)
            val iv = payload.copyOfRange(0, IV_SIZE)
            val encrypted = payload.copyOfRange(IV_SIZE, payload.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
            cipher.doFinal(encrypted).toString(Charsets.UTF_8)
        }.getOrNull()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val PREFERENCES = "cinavault_secure_session"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "cinavault_android_v2_build_2_session"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE = 12
        const val KEY_ENDPOINT = "endpoint"
        const val KEY_TOKEN = "token"
        const val KEY_EMAIL = "email"
        const val KEY_EXPIRES_AT = "expires_at"
        const val KEY_PERMISSIONS = "permissions"
    }
}
