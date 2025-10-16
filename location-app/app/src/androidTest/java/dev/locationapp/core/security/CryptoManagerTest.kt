package dev.locationapp.core.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoManagerTest {

    private lateinit var cryptoManager: CryptoManager

    @Before
    fun setup() {
        cryptoManager = CryptoManager()
    }

    @Test
    fun encryptAndDecryptReturnsOriginalText() {
        val originalText = "48.8566,2.3522"

        val encrypted = cryptoManager.encrypt(originalText)
        val decrypted = cryptoManager.decrypt(encrypted)

        assertEquals(originalText, decrypted)
        assertNotEquals(originalText, String(encrypted.data))
    }

    @Test
    fun encryptedDataHasValidIV() {
        val text = "test location data"

        val encrypted = cryptoManager.encrypt(text)

        assertNotNull(encrypted.iv)
        assertEquals(12, encrypted.iv.size)
    }

    @Test
    fun multipleEncryptionsProduceDifferentCiphertexts() {
        val text = "same text"

        val encrypted1 = cryptoManager.encrypt(text)
        val encrypted2 = cryptoManager.encrypt(text)

        assertFalse(encrypted1.data.contentEquals(encrypted2.data))
        assertFalse(encrypted1.iv.contentEquals(encrypted2.iv))
    }
}