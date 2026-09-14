package com.ai.altercode.data

import android.content.Context

/**
 * Generates and persists a stable, anonymous device identifier.
 *
 * - The ID is a random UUID created on first launch and stored in
 *   [EncryptedSharedPreferences] via [CryptoKeyManager].
 * - It survives app restarts but changes on reinstall, which is
 *   acceptable (a reinstall behaves as a fresh device).
 * - No hardware identifiers are used — this is privacy-respecting and
 *   does not require any runtime permissions.
 */
class DeviceFingerprint(context: Context) {

    private val prefs = CryptoKeyManager.encryptedPrefs(
        context.applicationContext,
        "altercode_device"
    )

    /** Stable anonymous device ID, created lazily on first access. */
    val deviceId: String by lazy {
        prefs.getString(KEY_DEVICE_ID, null) ?: run {
            val id = java.util.UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, id).apply()
            id
        }
    }

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
    }
}
