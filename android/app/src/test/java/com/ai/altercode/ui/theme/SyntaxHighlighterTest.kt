package com.ai.altercode.ui.theme

import androidx.compose.ui.graphics.Color
import com.ai.altercode.data.CodeLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SyntaxHighlighterTest {

    private val testColors = CodeColors(
        plain = Color.Black,
        keyword = Color.Red,
        string = Color.Green,
        number = Color.Blue,
        comment = Color.Gray,
        function = Color.Yellow,
        type = Color.Magenta,
        punctuation = Color.Cyan,
        gutter = Color.DarkGray
    )

    @Test
    fun testHighlightProducesAnnotatedStringWithSameText() {
        val code = "def hello():\n    # A comment\n    return \"world\" + 42"
        val result = SyntaxHighlighter.highlight(code, CodeLanguage.PYTHON, testColors)
        assertNotNull(result)
        assertEquals(code, result.text)
    }

    @Test
    fun testHighlightAllLanguages() {
        val snippet = "function test() { return 123; }"
        for (lang in CodeLanguage.entries) {
            val result = SyntaxHighlighter.highlight(snippet, lang, testColors)
            assertEquals(snippet, result.text)
        }
    }
}
