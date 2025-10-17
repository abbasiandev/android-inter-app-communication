package dev.locationapp.core.security.domain

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class FakeCryptoManager : ICryptoManager {
    private val secretKey = generateTestKey()

    override fun encrypt(plainText: String): EncryptedData {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return EncryptedData(encrypted, iv)
    }

    override fun decrypt(encryptedData: EncryptedData): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, encryptedData.iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decrypted = cipher.doFinal(encryptedData.data)
        return String(decrypted, Charsets.UTF_8)
    }

    override fun getDatabasePassphrase(): String = "test-passphrase-123"

    private fun generateTestKey(): SecretKeySpec {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val key = keyGen.generateKey()
        return SecretKeySpec(key.encoded, "AES")
    }
}
