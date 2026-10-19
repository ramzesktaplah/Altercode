package com.ai.altercode.ui.theme

import com.ai.altercode.data.CodeLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SyntaxHighlighterTest {

    private val colors = CodeColors()

    @Test
    fun testEmptyCode() {
        val result = SyntaxHighlighter.highlight("", CodeLanguage.PYTHON, colors)
        assertEquals("", result.text)
    }

    @Test
    fun testPythonKeywordsAndComments() {
        val code = "def hello():\n    # say hello\n    return \"world\""
        val result = SyntaxHighlighter.highlight(code, CodeLanguage.PYTHON, colors)
        assertEquals(code, result.text)

        // Check that styles were applied
        val spanStyles = result.spanStyles
        assertNotNull(spanStyles)
        // def, return should be keywords; comment should be comment style
        val keywordSpans = spanStyles.filter { it.item.color == colors.keyword }
        val commentSpans = spanStyles.filter { it.item.color == colors.comment }
        val stringSpans = spanStyles.filter { it.item.color == colors.string }

        assertEquals(2, keywordSpans.size) // def, return
        assertEquals(1, commentSpans.size) // # say hello
        assertEquals(1, stringSpans.size)  // "world"
    }

    @Test
    fun testRustKeywordsAndComments() {
        val code = "fn main() {\n    // comment\n    let mut x = 42;\n}"
        val result = SyntaxHighlighter.highlight(code, CodeLanguage.RUST, colors)
        assertEquals(code, result.text)

        val spanStyles = result.spanStyles
        val keywordSpans = spanStyles.filter { it.item.color == colors.keyword }
        val commentSpans = spanStyles.filter { it.item.color == colors.comment }
        val numberSpans = spanStyles.filter { it.item.color == colors.number }

        // fn, let, mut are keywords
        assertEquals(3, keywordSpans.size)
        assertEquals(1, commentSpans.size)
        assertEquals(1, numberSpans.size)
    }

    @Test
    fun testLuaComments() {
        val code = "local x = 10 -- lua comment"
        val result = SyntaxHighlighter.highlight(code, CodeLanguage.LUA, colors)
        assertEquals(code, result.text)

        val commentSpans = result.spanStyles.filter { it.item.color == colors.comment }
        assertEquals(1, commentSpans.size)
    }

    @Test
    fun testFunctionCallAndTypes() {
        val code = "val result: MyType = doSomething()"
        val result = SyntaxHighlighter.highlight(code, CodeLanguage.KOTLIN, colors)
        assertEquals(code, result.text)

        val functionSpans = result.spanStyles.filter { it.item.color == colors.function }
        val typeSpans = result.spanStyles.filter { it.item.color == colors.type }

        assertEquals(1, functionSpans.size) // doSomething
        assertEquals(1, typeSpans.size)     // MyType
    }
}
