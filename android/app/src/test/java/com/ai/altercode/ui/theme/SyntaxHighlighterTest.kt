package com.ai.altercode.ui.theme

import com.ai.altercode.data.CodeLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SyntaxHighlighterTest {

    private val colors = CodeColors()

    @Test
    fun testHighlightPreservesTextContent() {
        val snippet = """
            fun calculateSum(a: Int, b: Int): Int {
                // Line comment
                val result = a + b
                return result
            }
        """.trimIndent()

        val highlighted = SyntaxHighlighter.highlight(snippet, CodeLanguage.KOTLIN, colors)
        assertEquals(snippet, highlighted.text)
    }

    @Test
    fun testHighlightPythonLanguageKeywordsAndComments() {
        val snippet = """
            # Python script
            def process_data(items):
                if not items:
                    return None
                print("Processing")
                return True
        """.trimIndent()

        val highlighted = SyntaxHighlighter.highlight(snippet, CodeLanguage.PYTHON, colors)
        assertEquals(snippet, highlighted.text)
    }

    @Test
    fun testHighlightAllLanguagesWithoutError() {
        val sampleCode = """
            // Universal sample
            function test(a, b) {
                var x = 100;
                return a + b + x;
            }
        """.trimIndent()

        for (language in CodeLanguage.entries) {
            val highlighted = SyntaxHighlighter.highlight(sampleCode, language, colors)
            assertNotNull(highlighted)
            assertEquals(sampleCode, highlighted.text)
        }
    }
}
