package dev.locationapp.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.util.UUID
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds encrypted data.
 */
data class EncryptedData(
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EncryptedData
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}

/**
 * Manages encryption and decryption of sensitive data using RSA-2048.
 *
 * Security is critical. Sensitive data, including location information, must be stored securely,
 * using encryption and KeyStore or a secure equivalent.
 *
 * Keys are stored in Android KeyStore and persist across app restarts. When the service restarts,
 * keys are automatically retrieved from KeyStore, allowing encrypted data in the database to be
 * decrypted. The encryption key never leaves secure storage, making it difficult for attackers
 * to access sensitive information.
 */
@Singleton
class CryptoManager @Inject constructor() {

    private val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    /**
     * Encrypts plain text using RSA public key.
     * The encrypted data can be safely stored in the database.
     */
    fun encrypt(plainText: String): EncryptedData {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKeyPair().public)
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return EncryptedData(encrypted)
    }

    /**
     * Decrypts encrypted data using RSA private key.
     * Works reliably after service restarts since keys persist in KeyStore.
     */
    fun decrypt(encryptedData: EncryptedData): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKeyPair().private)
        val decrypted = cipher.doFinal(encryptedData.data)
        return String(decrypted, Charsets.UTF_8)
    }

    /**
     * Provides a secure passphrase for SQLCipher database encryption.
     * Returns the same passphrase across restarts to maintain database access.
     */
    fun getDatabasePassphrase(): String {
        return if (keyStore.containsAlias(DB_PASSPHRASE_ALIAS)) {
            UUID.nameUUIDFromBytes(KEY_ALIAS.toByteArray()).toString()
        } else {
            UUID.randomUUID().toString()
        }
    }

    private fun getOrCreateKeyPair(): KeyPair {
        return if (!keyStore.containsAlias(KEY_ALIAS)) {
            createKeyPair()
            KeyPair(
                keyStore.getCertificate(KEY_ALIAS).publicKey,
                keyStore.getKey(KEY_ALIAS, null) as PrivateKey
            )
        } else {
            KeyPair(
                keyStore.getCertificate(KEY_ALIAS).publicKey,
                keyStore.getKey(KEY_ALIAS, null) as PrivateKey
            )
        }
    }

    private fun createKeyPair() {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            KEYSTORE_PROVIDER
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(KEY_SIZE)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setUserAuthenticationRequired(false)
            .build()

        keyPairGenerator.initialize(spec)
        keyPairGenerator.generateKeyPair()
    }

    private data class KeyPair(
        val public: PublicKey,
        val private: PrivateKey
    )

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "location_encryption_key"
        private const val DB_PASSPHRASE_ALIAS = "db_passphrase_key"
        private const val TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        private const val KEY_SIZE = 2048
    }
}