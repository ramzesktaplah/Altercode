package com.ai.altercode.data

/** The four AI operations AlterCode can run on a snippet. */
enum class CodeAction(
    val id: String,
    val label: String,
    val pastLabel: String
) {
    CONVERT("convert", "Convert", "Converted"),
    REFACTOR("refactor", "Refactor", "Refactored"),
    FIX("fix", "Fix Bugs", "Bug-fixed"),
    EXPLAIN("explain", "Explain", "Explained");

    /** Explanations are prose, everything else returns runnable code. */
    val producesCode: Boolean get() = this != EXPLAIN

    companion object {
        fun fromId(id: String?): CodeAction = entries.firstOrNull { it.id == id } ?: CONVERT
    }
}

/** Optional prompt presets the user can toggle in Settings. */
data class PromptPresets(
    val focusOnSpeed: Boolean = false,
    val focusOnReadability: Boolean = true,
    val addInlineComments: Boolean = false
)
