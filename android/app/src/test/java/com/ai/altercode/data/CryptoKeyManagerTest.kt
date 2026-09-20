package com.ai.altercode.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class CryptoKeyManagerTest {

    @Test
    fun testClearCachedPassphraseZeroesArray() {
        val testBytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)

        // Reflectively set cachedPassphrase to test array
        val field = CryptoKeyManager::class.java.getDeclaredField("cachedPassphrase")
        field.isAccessible = true
        field.set(CryptoKeyManager, testBytes)

        CryptoKeyManager.clearCachedPassphrase()

        // Verify the original byte array was wiped (zeroed)
        val expectedZeroes = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
        assertArrayEquals(expectedZeroes, testBytes)

        // Verify field was set to null
        val valueAfter = field.get(CryptoKeyManager)
        assertEquals(null, valueAfter)
    }
}
