package dev.locationapp.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

data class EncryptedData(
    val data: ByteArray,
    val iv: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EncryptedData
        if (!data.contentEquals(other.data)) return false
        if (!iv.contentEquals(other.iv)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}

@Singleton
class CryptoManager
    @Inject
    constructor() {
        private val keyStore =
            KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
                load(null)
            }

        /**
         * Encrypt plain text using AES-256-GCM
         */
        fun encrypt(plainText: String): EncryptedData {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            return EncryptedData(encrypted, iv)
        }

        /**
         * Decrypt encrypted data
         */
        fun decrypt(encryptedData: EncryptedData): String {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, encryptedData.iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)
            val decrypted = cipher.doFinal(encryptedData.data)
            return String(decrypted, Charsets.UTF_8)
        }

        /**
         * Get passphrase for SQLCipher database encryption
         */
        fun getDatabasePassphrase(): String =
            if (keyStore.containsAlias(DB_PASSPHRASE_ALIAS)) {
                // retrieve existing passphrase
                UUID.nameUUIDFromBytes(KEY_ALIAS.toByteArray()).toString()
            } else {
                UUID.randomUUID().toString().also {
                    // for store reference in production
                }
            }

        private fun getOrCreateKey(): SecretKey {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                createKey()
            }
            return keyStore.getKey(KEY_ALIAS, null) as SecretKey
        }

        private fun createKey() {
            val keyGenerator =
                KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    KEYSTORE_PROVIDER,
                )

            val spec =
                KeyGenParameterSpec
                    .Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }

        companion object {
            private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
            private const val KEY_ALIAS = "location_encryption_key"
            private const val DB_PASSPHRASE_ALIAS = "db_passphrase_key"
            private const val TRANSFORMATION = "AES/GCM/NoPadding"
            private const val GCM_TAG_LENGTH = 128
            private const val KEY_SIZE = 256
        }
    }
