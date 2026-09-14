package com.ai.altercode.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.altercode.ServiceLocator
import com.ai.altercode.ads.RewardedAdManager
import com.ai.altercode.ai.AiCodeException
import com.ai.altercode.ai.AiCodeService
import com.ai.altercode.data.ClipboardTools
import com.ai.altercode.data.CodeAction
import com.ai.altercode.data.CodeLanguage
import com.ai.altercode.data.EditorSession
import com.ai.altercode.data.NewSnippet
import com.ai.altercode.data.SettingsRepository
import com.ai.altercode.data.SnippetRepository
import com.ai.altercode.data.UsageTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val code: String = "",
    val sourceLanguage: CodeLanguage = CodeLanguage.AUTO,
    val targetLanguage: CodeLanguage = CodeLanguage.TYPESCRIPT,
    val detectedLanguage: CodeLanguage? = null,
    val runningAction: CodeAction? = null,
    val error: String? = null,
    val notice: String? = null,
    val isEditorExpanded: Boolean = false,
    val freeRunsRemaining: Int = UsageTracker.FREE_RUNS,
    val isAdGateActive: Boolean = false,
    val showAdGateDialog: Boolean = false,
    val isAdLoading: Boolean = false
) {
    val isRunning: Boolean get() = runningAction != null

    /** Language used to colorize the editor contents. */
    val editorLanguage: CodeLanguage
        get() = when {
            sourceLanguage.isConcrete -> sourceLanguage
            detectedLanguage != null -> detectedLanguage
            else -> CodeLanguage.TYPESCRIPT
        }
}

class HomeViewModel(
    private val snippets: SnippetRepository = ServiceLocator.snippets,
    private val settings: SettingsRepository = ServiceLocator.settings,
    private val ai: AiCodeService = ServiceLocator.ai,
    private val editorSession: EditorSession = ServiceLocator.editorSession,
    private val usageTracker: UsageTracker = ServiceLocator.usageTracker,
    private val rewardedAds: RewardedAdManager = ServiceLocator.rewardedAds
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            // Restore the locally cached draft so in-progress code survives
            // app restarts and is editable offline.
            code = settings.editorDraftCode,
            sourceLanguage = CodeLanguage.fromId(settings.lastSourceLanguageId) ?: CodeLanguage.AUTO,
            targetLanguage = CodeLanguage.fromId(settings.lastTargetLanguageId)
                ?: CodeLanguage.TYPESCRIPT,
            freeRunsRemaining = usageTracker.freeRunsRemaining,
            isAdGateActive = usageTracker.isAdGateActive
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _openSnippet = MutableStateFlow<Long?>(null)
    val openSnippet: StateFlow<Long?> = _openSnippet.asStateFlow()

    /** The action the user tried to run when the ad gate was active. */
    private var pendingAction: CodeAction? = null

    /** Debounces draft writes so every keystroke doesn't hit encrypted prefs. */
    private var draftSaveJob: Job? = null

    init {
        viewModelScope.launch {
            editorSession.pending.collect { handoff ->
                if (handoff == null) return@collect
                _uiState.update {
                    it.copy(
                        code = handoff.code,
                        sourceLanguage = handoff.sourceLanguage,
                        targetLanguage = handoff.targetLanguage,
                        error = null,
                        notice = null
                    )
                }
                scheduleDraftSave(handoff.code)
                editorSession.consume()
                if (handoff.runImmediately) run(handoff.action)
            }
        }

        // Keep ad-loading state in sync so the UI can show a spinner on the "Watch Ad" button.
        viewModelScope.launch {
            rewardedAds.isAdLoading.collect { loading ->
                _uiState.update { it.copy(isAdLoading = loading) }
            }
        }
    }

    fun setCode(code: String) {
        _uiState.update { it.copy(code = code, error = null) }
        scheduleDraftSave(code)
    }

    /** Caches the editor contents locally (debounced) for offline restarts. */
    private fun scheduleDraftSave(code: String) {
        draftSaveJob?.cancel()
        draftSaveJob = viewModelScope.launch {
            delay(DRAFT_SAVE_DEBOUNCE_MS)
            settings.editorDraftCode = code
        }
    }

    fun setSourceLanguage(language: CodeLanguage) {
        settings.lastSourceLanguageId = language.id
        _uiState.update { it.copy(sourceLanguage = language, detectedLanguage = null) }
    }

    fun setTargetLanguage(language: CodeLanguage) {
        settings.lastTargetLanguageId = language.id
        _uiState.update { it.copy(targetLanguage = language) }
    }

    fun toggleEditorExpanded() {
        _uiState.update { it.copy(isEditorExpanded = !it.isEditorExpanded) }
    }

    fun clearCode() {
        draftSaveJob?.cancel()
        settings.editorDraftCode = ""
        _uiState.update {
            it.copy(code = "", detectedLanguage = null, error = null, notice = "Editor cleared")
        }
    }

    fun pasteFromClipboard() {
        val result = ClipboardTools.pasteWithMeta(ServiceLocator.appContext)
        if (result == null) {
            _uiState.update { it.copy(notice = "Clipboard is empty") }
            return
        }
        val notice = if (result.truncated) {
            "Pasted from clipboard (truncated — too long)"
        } else {
            "Pasted from clipboard"
        }
        _uiState.update {
            it.copy(code = result.text, detectedLanguage = null, error = null, notice = notice)
        }
        scheduleDraftSave(result.text)
    }

    fun dismissNotice() {
        _uiState.update { it.copy(notice = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun consumeOpenSnippet() {
        _openSnippet.value = null
    }

    fun dismissAdGateDialog() {
        _uiState.update { it.copy(showAdGateDialog = false) }
        pendingAction = null
    }

    /**
     * Called by the UI when the user taps "Watch Ad" in the gate dialog.
     * The [activity] is needed by the AdMob SDK to display the full-screen ad.
     * After the reward is earned, the pending action (if any) is executed.
     */
    fun watchAdAndContinue(activity: android.app.Activity) {
        rewardedAds.showAd(activity) {
            // Reward granted (or ad was unavailable and we gave a free pass).
            _uiState.update {
                it.copy(
                    freeRunsRemaining = usageTracker.freeRunsRemaining,
                    isAdGateActive = usageTracker.isAdGateActive,
                    showAdGateDialog = false,
                    notice = "+${UsageTracker.REWARD_RUNS} free runs unlocked!"
                )
            }
            val action = pendingAction
            pendingAction = null
            if (action != null) {
                run(action)
            }
        }
    }

    fun run(action: CodeAction) {
        val state = _uiState.value
        if (state.isRunning) return
        if (state.code.isBlank()) {
            _uiState.update { it.copy(error = "Add some code first, or paste it from your clipboard.") }
            return
        }
        if (!ClipboardTools.isOnline(ServiceLocator.appContext)) {
            _uiState.update {
                it.copy(error = "You're offline. Your code is safe — reconnect and try again.")
            }
            return
        }
        if (action == CodeAction.CONVERT &&
            state.sourceLanguage.isConcrete &&
            state.sourceLanguage == state.targetLanguage
        ) {
            _uiState.update { it.copy(error = "Pick a different target language to convert into.") }
            return
        }

        // Ad gate: check if the user has free runs remaining.
        if (!usageTracker.canRun()) {
            pendingAction = action
            _uiState.update {
                it.copy(
                    showAdGateDialog = true,
                    error = null,
                    notice = null
                )
            }
            // Try to preload an ad if one isn't ready yet.
            if (!rewardedAds.hasAdReady()) {
                rewardedAds.loadAd()
            }
            return
        }

        // Consume a free run.
        usageTracker.recordRun()

        _uiState.update {
            it.copy(
                runningAction = action,
                error = null,
                notice = null,
                freeRunsRemaining = usageTracker.freeRunsRemaining,
                isAdGateActive = usageTracker.isAdGateActive
            )
        }

        viewModelScope.launch {
            try {
                val result = ai.run(
                    action = action,
                    code = state.code,
                    sourceLanguage = state.sourceLanguage,
                    targetLanguage = state.targetLanguage,
                    presets = settings.presets.value
                )
                val resolvedSource = when {
                    state.sourceLanguage.isConcrete -> state.sourceLanguage
                    else -> result.detectedLanguage ?: CodeLanguage.AUTO
                }
                val id = snippets.save(
                    NewSnippet(
                        title = result.title,
                        action = action,
                        sourceLanguage = resolvedSource,
                        targetLanguage = if (action == CodeAction.CONVERT) state.targetLanguage else null,
                        sourceCode = state.code,
                        result = result.body,
                        summary = result.summary
                    )
                )
                _uiState.update {
                    it.copy(runningAction = null, detectedLanguage = result.detectedLanguage)
                }
                _openSnippet.value = id
            } catch (error: AiCodeException) {
                // Refund the run on failure so the user doesn't lose a free attempt.
                usageTracker.refundRun()
                _uiState.update {
                    it.copy(
                        runningAction = null,
                        error = error.userMessage,
                        freeRunsRemaining = usageTracker.freeRunsRemaining,
                        isAdGateActive = usageTracker.isAdGateActive
                    )
                }
            } catch (error: Exception) {
                usageTracker.refundRun()
                _uiState.update {
                    it.copy(
                        runningAction = null,
                        error = "Something went wrong. Please try again.",
                        freeRunsRemaining = usageTracker.freeRunsRemaining,
                        isAdGateActive = usageTracker.isAdGateActive
                    )
                }
            }
        }
    }

    private companion object {
        const val DRAFT_SAVE_DEBOUNCE_MS = 400L
    }
}
