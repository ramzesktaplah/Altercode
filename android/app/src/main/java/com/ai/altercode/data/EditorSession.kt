package com.ai.altercode.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A request to load code back into the Home editor (e.g. "Run Again" from a snippet). */
data class EditorHandoff(
    val code: String,
    val sourceLanguage: CodeLanguage,
    val targetLanguage: CodeLanguage,
    val action: CodeAction,
    val runImmediately: Boolean
)

/** Bridges snippet detail actions back into the Home editor without prop drilling. */
class EditorSession {
    private val _pending = MutableStateFlow<EditorHandoff?>(null)
    val pending: StateFlow<EditorHandoff?> = _pending.asStateFlow()

    fun request(handoff: EditorHandoff) {
        _pending.value = handoff
    }

    fun consume() {
        _pending.value = null
    }
}
