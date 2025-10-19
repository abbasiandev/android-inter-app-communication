package dev.locationapp.core.security.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dev.locationapp.core.security.domain.EncryptedData
import dev.locationapp.core.security.domain.ICryptoManager
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager
    @Inject
    constructor() : ICryptoManager {
        private val keyStore =
            KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
                load(null)
            }

        override fun encrypt(plainText: String): EncryptedData {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            return EncryptedData(encrypted, iv)
        }

        override fun decrypt(encryptedData: EncryptedData): String {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, encryptedData.iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)
            val decrypted = cipher.doFinal(encryptedData.data)
            return String(decrypted, Charsets.UTF_8)
        }

        override fun getDatabasePassphrase(): String = UUID.nameUUIDFromBytes(KEY_ALIAS.toByteArray()).toString()

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
            private const val TRANSFORMATION = "AES/GCM/NoPadding"
            private const val GCM_TAG_LENGTH = 128
            private const val KEY_SIZE = 256
        }
    }
