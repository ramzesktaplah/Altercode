package com.ai.altercode.data

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages cryptographic keys via the Android Keystore and provides
 * encrypted storage helpers for the snippet database and preferences.
 *
 * - AES-256 keys are generated inside the Keystore (hardware-backed when available).
 * - The SQLCipher database passphrase is encrypted with the Keystore key and
 *   stored as a Base64 blob in a regular (non-encrypted) SharedPreferences file.
 * - Preference files use [EncryptedSharedPreferences] backed by a [MasterKey].
 */
object CryptoKeyManager {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val DB_KEY_ALIAS = "altercode_db_key"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    private const val PASSPHRASE_PREFS = "altercode_secure_prefs"
    private const val PASSPHRASE_KEY = "db_passphrase_blob"

    private const val ENCRYPTED_PREFS_PREFIX = "altercode_enc_"

    private var cachedPassphrase: ByteArray? = null

    /**
     * Returns the SQLCipher database passphrase, generating and persisting
     * it on first call. The passphrase itself is encrypted at rest with a
     * Keystore-backed AES key.
     */
    @Synchronized
    fun getDatabasePassphrase(context: Context): ByteArray {
        cachedPassphrase?.let { return it }

        val prefs = context.applicationContext
            .getSharedPreferences(PASSPHRASE_PREFS, Context.MODE_PRIVATE)

        val stored = prefs.getString(PASSPHRASE_KEY, null)
        val passphrase = if (stored != null) {
            decryptWithKeystoreKey(stored)
        } else {
            val generated = generateRandomPassphrase()
            val encrypted = encryptWithKeystoreKey(generated)
            prefs.edit().putString(PASSPHRASE_KEY, encrypted).apply()
            generated
        }

        cachedPassphrase = passphrase
        return passphrase
    }

    /**
     * Creates (or opens) an [EncryptedSharedPreferences] file backed by a
     * Keystore [MasterKey]. Used by [SettingsRepository] and [UsageTracker].
     */
    fun encryptedPrefs(context: Context, fileName: String): SharedPreferences {
        val appContext = context.applicationContext
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            appContext,
            ENCRYPTED_PREFS_PREFIX + fileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ---- Keystore AES key for DB passphrase encryption ----

    private fun getOrCreateKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existing = keyStore.getKey(DB_KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER
        )
        val spec = KeyGenParameterSpec.Builder(
            DB_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    private fun encryptWithKeystoreKey(plaintext: ByteArray): String {
        val key = getOrCreateKeystoreKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)
        // Prepend IV (Base64-encoded) to the ciphertext, separated by ':'
        val combined = iv + ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decryptWithKeystoreKey(encryptedBlob: String): ByteArray {
        val key = getOrCreateKeystoreKey()
        val combined = Base64.decode(encryptedBlob, Base64.NO_WRAP)
        require(combined.size > GCM_IV_LENGTH) { "Invalid encrypted blob" }
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(ciphertext)
    }

    private fun generateRandomPassphrase(): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes
    }
}
