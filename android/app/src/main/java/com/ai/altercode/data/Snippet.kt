package com.ai.altercode.data

/** A single saved AI result, persisted in the on-device SQLite database. */
data class Snippet(
    val id: Long,
    val title: String,
    val action: CodeAction,
    val sourceLanguage: CodeLanguage,
    val targetLanguage: CodeLanguage?,
    val sourceCode: String,
    val result: String,
    val summary: String,
    val isFavorite: Boolean,
    val createdAt: Long
) {
    /** Language used to colorize the result body. */
    val resultLanguage: CodeLanguage
        get() = when {
            !action.producesCode -> sourceLanguage
            action == CodeAction.CONVERT -> targetLanguage ?: sourceLanguage
            else -> sourceLanguage
        }

    fun matches(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return title.lowercase().contains(q) ||
            sourceLanguage.label.lowercase().contains(q) ||
            (targetLanguage?.label?.lowercase()?.contains(q) == true) ||
            action.label.lowercase().contains(q) ||
            result.lowercase().contains(q) ||
            sourceCode.lowercase().contains(q)
    }
}

/** Draft passed to the repository before an id exists. */
data class NewSnippet(
    val title: String,
    val action: CodeAction,
    val sourceLanguage: CodeLanguage,
    val targetLanguage: CodeLanguage?,
    val sourceCode: String,
    val result: String,
    val summary: String
)
