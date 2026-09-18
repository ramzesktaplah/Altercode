package com.ai.altercode.ui.theme

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.ai.altercode.data.CodeLanguage

/**
 * Small, dependency-free tokenizer that colorizes code for display and editing.
 * Handles comments, strings, numbers, keywords, call sites, and types.
 */
object SyntaxHighlighter {

    private val commonKeywords = setOf(
        "if", "else", "for", "while", "return", "break", "continue", "switch", "case",
        "default", "class", "struct", "enum", "interface", "new", "try", "catch", "finally",
        "throw", "throws", "import", "from", "as", "in", "is", "not", "and", "or", "null",
        "true", "false", "this", "self", "super", "public", "private", "protected", "static",
        "final", "const", "let", "var", "function", "def", "lambda", "async", "await", "yield",
        "type", "extends", "implements", "package", "namespace", "using", "void", "int",
        "float", "double", "bool", "boolean", "string", "char", "long", "short", "elif",
        "fun", "val", "fn", "func", "mut", "pub", "impl", "trait", "match", "where", "with",
        "do", "then", "end", "pass", "raise", "except", "global", "nonlocal", "del",
        "typeof", "instanceof", "delete", "export", "default:", "guard", "defer", "extension",
        "protocol", "override", "init", "deinit", "inline", "suspend", "data", "object",
        "sealed", "when", "companion", "operator", "readonly", "abstract", "virtual"
    )

    // Performance optimization: Pre-calculated keyword sets per language to prevent allocation of new Sets on every render/keystroke.
    private val pythonKeywords = commonKeywords + setOf("def", "elif", "None", "True", "False", "print")
    private val rustKeywords = commonKeywords + setOf("fn", "let", "mut", "impl", "crate", "Some", "None", "Ok", "Err")
    private val goKeywords = commonKeywords + setOf("func", "go", "defer", "chan", "range", "nil", "map")
    private val swiftKeywords = commonKeywords + setOf("guard", "let", "var", "func", "nil", "some", "any")
    private val phpKeywords = commonKeywords + setOf("echo", "elseif", "foreach", "endif", "array")

    // Pre-calculated comment token lists to prevent allocation of new Lists on every syntax highlighting pass.
    private val hashCommentTokens = listOf("#")
    private val slashAndHashCommentTokens = listOf("//", "#")
    private val slashCommentTokens = listOf("//")

    private fun keywords(language: CodeLanguage): Set<String> = when (language) {
        CodeLanguage.PYTHON -> pythonKeywords
        CodeLanguage.RUST -> rustKeywords
        CodeLanguage.GO -> goKeywords
        CodeLanguage.SWIFT -> swiftKeywords
        CodeLanguage.PHP -> phpKeywords
        else -> commonKeywords
    }

    private fun lineCommentTokens(language: CodeLanguage): List<String> = when (language) {
        CodeLanguage.PYTHON -> hashCommentTokens
        CodeLanguage.PHP -> slashAndHashCommentTokens
        else -> slashCommentTokens
    }

    fun highlight(
        code: String,
        language: CodeLanguage,
        colors: CodeColors
    ): AnnotatedString = buildAnnotatedString {
        val keywordSet = keywords(language)
        val lineComments = lineCommentTokens(language)
        var index = 0
        val length = code.length

        while (index < length) {
            val char = code[index]

            // Block comments
            if (char == '/' && index + 1 < length && code[index + 1] == '*') {
                val end = code.indexOf("*/", index + 2).let { if (it < 0) length else it + 2 }
                appendStyled(code.substring(index, end), colors.comment)
                index = end
                continue
            }

            // Line comments
            val lineComment = lineComments.firstOrNull { code.startsWith(it, index) }
            if (lineComment != null) {
                val end = code.indexOf('\n', index).let { if (it < 0) length else it }
                appendStyled(code.substring(index, end), colors.comment)
                index = end
                continue
            }

            // Python docstrings / template literals / strings
            if (char == '"' || char == '\'' || char == '`') {
                val tripleQuote = code.startsWith("\"\"\"", index) || code.startsWith("'''", index)
                val end = if (tripleQuote) {
                    val marker = code.substring(index, index + 3)
                    code.indexOf(marker, index + 3).let { if (it < 0) length else it + 3 }
                } else {
                    scanString(code, index, char)
                }
                appendStyled(code.substring(index, end), colors.string)
                index = end
                continue
            }

            // Numbers
            if (char.isDigit()) {
                var end = index
                while (end < length && (code[end].isLetterOrDigit() || code[end] == '.' || code[end] == '_')) {
                    end++
                }
                appendStyled(code.substring(index, end), colors.number)
                index = end
                continue
            }

            // Identifiers / keywords
            if (char.isLetter() || char == '_' || char == '$' || char == '@' || char == '#') {
                var end = index
                while (end < length && (code[end].isLetterOrDigit() || code[end] == '_' || code[end] == '$' || (end == index && (code[end] == '@' || code[end] == '#')))) {
                    end++
                }
                val word = code.substring(index, end)
                var lookahead = end
                while (lookahead < length && code[lookahead] == ' ') lookahead++
                val isCall = lookahead < length && code[lookahead] == '('
                val color = when {
                    keywordSet.contains(word) -> colors.keyword
                    word.startsWith("@") || word.startsWith("#") -> colors.keyword
                    isCall -> colors.function
                    word.first().isUpperCase() -> colors.type
                    else -> colors.plain
                }
                appendStyled(word, color)
                index = end
                continue
            }

            // Whitespace passes through untouched to keep the editor cursor stable
            if (char.isWhitespace()) {
                append(char)
                index++
                continue
            }

            appendStyled(char.toString(), colors.punctuation)
            index++
        }
    }

    private fun scanString(code: String, start: Int, quote: Char): Int {
        var index = start + 1
        while (index < code.length) {
            val current = code[index]
            if (current == '\\') {
                index += 2
                continue
            }
            if (current == quote) return index + 1
            if (current == '\n' && quote != '`') return index
            index++
        }
        return code.length
    }

    private fun androidx.compose.ui.text.AnnotatedString.Builder.appendStyled(
        text: String,
        color: androidx.compose.ui.graphics.Color
    ) {
        withStyle(SpanStyle(color = color)) { append(text) }
    }
}
