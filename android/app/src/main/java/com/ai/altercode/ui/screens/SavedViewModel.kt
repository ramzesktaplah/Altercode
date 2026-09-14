package com.ai.altercode.ui.screens

import androidx.lifecycle.ViewModel
import com.ai.altercode.ServiceLocator
import com.ai.altercode.data.CodeLanguage
import com.ai.altercode.data.Snippet
import com.ai.altercode.data.SnippetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Active list filter on the Saved tab. */
sealed interface SavedFilter {
    data object All : SavedFilter
    data object Favorites : SavedFilter
    data class Language(val language: CodeLanguage) : SavedFilter
}

class SavedViewModel(
    private val repository: SnippetRepository = ServiceLocator.snippets
) : ViewModel() {

    val snippets: StateFlow<List<Snippet>> = repository.snippets

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow<SavedFilter>(SavedFilter.All)
    val filter: StateFlow<SavedFilter> = _filter.asStateFlow()

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setFilter(filter: SavedFilter) {
        _filter.value = if (_filter.value == filter) SavedFilter.All else filter
    }

    fun toggleFavorite(id: Long) = repository.toggleFavorite(id)

    fun delete(id: Long) = repository.delete(id)

    /** Languages present in history, used to build the filter chip row. */
    fun availableLanguages(items: List<Snippet>): List<CodeLanguage> =
        items.flatMap { listOfNotNull(it.sourceLanguage.takeIf { lang -> lang.isConcrete }, it.targetLanguage) }
            .distinct()
            .sortedBy { it.label }

    fun applyFilters(
        items: List<Snippet>,
        query: String,
        filter: SavedFilter
    ): List<Snippet> = items.filter { snippet ->
        val matchesQuery = snippet.matches(query)
        val matchesFilter = when (filter) {
            SavedFilter.All -> true
            SavedFilter.Favorites -> snippet.isFavorite
            is SavedFilter.Language ->
                snippet.sourceLanguage == filter.language || snippet.targetLanguage == filter.language
        }
        matchesQuery && matchesFilter
    }
}
