package com.ai.altercode.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Single source of truth for saved snippets, backed by local SQLite. */
class SnippetRepository(context: Context) {

    private val db = SnippetDatabase.create(context.applicationContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _snippets = MutableStateFlow<List<Snippet>>(emptyList())
    val snippets: StateFlow<List<Snippet>> = _snippets.asStateFlow()

    init {
        scope.launch { reload() }
    }

    private suspend fun reload() {
        runCatching { withContext(Dispatchers.IO) { db.queryAll() } }
            .onSuccess { _snippets.value = it }
            .onFailure { Log.e(TAG, "Failed to load snippet history") }
    }

    suspend fun save(draft: NewSnippet): Long {
        val id = withContext(Dispatchers.IO) { db.insert(draft, System.currentTimeMillis()) }
        reload()
        return id
    }

    fun toggleFavorite(id: Long) {
        val current = _snippets.value.firstOrNull { it.id == id } ?: return
        scope.launch {
            withContext(Dispatchers.IO) { db.setFavorite(id, !current.isFavorite) }
            reload()
        }
    }

    fun delete(id: Long) {
        scope.launch {
            withContext(Dispatchers.IO) { db.delete(id) }
            reload()
        }
    }

    fun clearAll() {
        scope.launch {
            withContext(Dispatchers.IO) { db.deleteAll() }
            reload()
        }
    }

    fun snippetById(id: Long): Snippet? = _snippets.value.firstOrNull { it.id == id }

    private companion object {
        const val TAG = "SnippetRepository"
    }
}
