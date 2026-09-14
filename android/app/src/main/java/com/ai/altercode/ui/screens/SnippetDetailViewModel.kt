package com.ai.altercode.ui.screens

import androidx.lifecycle.ViewModel
import com.ai.altercode.ServiceLocator
import com.ai.altercode.data.CodeLanguage
import com.ai.altercode.data.EditorHandoff
import com.ai.altercode.data.EditorSession
import com.ai.altercode.data.Snippet
import com.ai.altercode.data.SnippetRepository
import kotlinx.coroutines.flow.StateFlow

class SnippetDetailViewModel(
    private val repository: SnippetRepository = ServiceLocator.snippets,
    private val editorSession: EditorSession = ServiceLocator.editorSession
) : ViewModel() {

    val snippets: StateFlow<List<Snippet>> = repository.snippets

    fun toggleFavorite(id: Long) = repository.toggleFavorite(id)

    /** Sends the original code back to the Home editor and re-runs the same action. */
    fun runAgain(snippet: Snippet) {
        editorSession.request(
            EditorHandoff(
                code = snippet.sourceCode,
                sourceLanguage = snippet.sourceLanguage,
                targetLanguage = snippet.targetLanguage
                    ?: snippet.sourceLanguage.takeIf { it.isConcrete }
                    ?: CodeLanguage.TYPESCRIPT,
                action = snippet.action,
                runImmediately = true
            )
        )
    }
}
