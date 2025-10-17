package dev.locationapp.core.security.domain

interface ICryptoManager {
    fun encrypt(plainText: String): EncryptedData

    fun decrypt(encryptedData: EncryptedData): String

    fun getDatabasePassphrase(): String
}
