package com.ai.altercode.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.altercode.ServiceLocator
import com.ai.altercode.ai.AiCodeService
import com.ai.altercode.data.PromptPresets
import com.ai.altercode.data.SettingsRepository
import com.ai.altercode.data.SnippetRepository
import com.ai.altercode.data.UsageTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository = ServiceLocator.settings,
    private val snippets: SnippetRepository = ServiceLocator.snippets,
    private val usageTracker: UsageTracker = ServiceLocator.usageTracker
) : ViewModel() {

    val presets: StateFlow<PromptPresets> = settings.presets

    private val _snippetCount = MutableStateFlow(0)
    val snippetCount: StateFlow<Int> = _snippetCount.asStateFlow()

    private val _usageState = MutableStateFlow(UsageUiState())
    val usageState: StateFlow<UsageUiState> = _usageState.asStateFlow()

    /** Human-readable name of the model powering AI actions. */
    val modelName: String = AiCodeService.PRIMARY_MODEL.substringAfter('/')

    init {
        viewModelScope.launch {
            snippets.snippets.collect { list -> _snippetCount.value = list.size }
        }
        viewModelScope.launch {
            usageTracker.runsUsed.collect { used ->
                _usageState.value = _usageState.value.copy(
                    freeRunsRemaining = (UsageTracker.FREE_RUNS - used).coerceAtLeast(0),
                    isAdGateActive = used >= UsageTracker.FREE_RUNS
                )
            }
        }
        viewModelScope.launch {
            usageTracker.totalRuns.collect { total ->
                _usageState.value = _usageState.value.copy(totalRuns = total)
            }
        }
        viewModelScope.launch {
            usageTracker.totalAdsWatched.collect { ads ->
                _usageState.value = _usageState.value.copy(adsWatched = ads)
            }
        }
    }

    fun setFocusOnSpeed(value: Boolean) {
        settings.updatePresets(presets.value.copy(focusOnSpeed = value))
    }

    fun setFocusOnReadability(value: Boolean) {
        settings.updatePresets(presets.value.copy(focusOnReadability = value))
    }

    fun setAddInlineComments(value: Boolean) {
        settings.updatePresets(presets.value.copy(addInlineComments = value))
    }

    fun clearHistory() = snippets.clearAll()

    fun resetUsage() = usageTracker.reset()
}

data class UsageUiState(
    val freeRunsRemaining: Int = UsageTracker.FREE_RUNS,
    val isAdGateActive: Boolean = false,
    val totalRuns: Int = 0,
    val adsWatched: Int = 0
)
