package com.ai.altercode.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Lightweight persisted preferences: onboarding state, prompt presets, last languages. */
class SettingsRepository(context: Context) {

    private val prefs = CryptoKeyManager.encryptedPrefs(context.applicationContext, "altercode_settings")

    private val _presets = MutableStateFlow(
        PromptPresets(
            focusOnSpeed = prefs.getBoolean(KEY_SPEED, false),
            focusOnReadability = prefs.getBoolean(KEY_READABILITY, true),
            addInlineComments = prefs.getBoolean(KEY_COMMENTS, false)
        )
    )
    val presets: StateFlow<PromptPresets> = _presets.asStateFlow()

    val hasOnboarded: Boolean get() = prefs.getBoolean(KEY_ONBOARDED, false)

    fun markOnboarded() {
        prefs.edit().putBoolean(KEY_ONBOARDED, true).apply()
    }

    fun updatePresets(presets: PromptPresets) {
        _presets.value = presets
        prefs.edit()
            .putBoolean(KEY_SPEED, presets.focusOnSpeed)
            .putBoolean(KEY_READABILITY, presets.focusOnReadability)
            .putBoolean(KEY_COMMENTS, presets.addInlineComments)
            .apply()
    }

    var lastSourceLanguageId: String
        get() = prefs.getString(KEY_LAST_SOURCE, CodeLanguage.AUTO.id) ?: CodeLanguage.AUTO.id
        set(value) = prefs.edit().putString(KEY_LAST_SOURCE, value).apply()

    var lastTargetLanguageId: String
        get() = prefs.getString(KEY_LAST_TARGET, CodeLanguage.TYPESCRIPT.id)
            ?: CodeLanguage.TYPESCRIPT.id
        set(value) = prefs.edit().putString(KEY_LAST_TARGET, value).apply()

    /**
     * Unsaved editor draft, cached locally so in-progress code survives app
     * restarts and offline sessions. Stored in encrypted prefs (never synced).
     */
    var editorDraftCode: String
        get() = prefs.getString(KEY_DRAFT_CODE, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_DRAFT_CODE, value).apply()

    private companion object {
        const val KEY_ONBOARDED = "has_onboarded"
        const val KEY_SPEED = "preset_speed"
        const val KEY_READABILITY = "preset_readability"
        const val KEY_COMMENTS = "preset_comments"
        const val KEY_LAST_SOURCE = "last_source_lang"
        const val KEY_LAST_TARGET = "last_target_lang"
        const val KEY_DRAFT_CODE = "editor_draft_code"
    }
}
