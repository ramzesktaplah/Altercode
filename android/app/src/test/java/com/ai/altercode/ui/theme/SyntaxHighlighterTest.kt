package com.ai.altercode.ui.theme

import androidx.compose.ui.graphics.Color
import com.ai.altercode.data.CodeLanguage

/**
 * Verification test suite for [SyntaxHighlighter].
 * Executed during unit tests/build verification.
 */
class SyntaxHighlighterTest {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val test = SyntaxHighlighterTest()
            test.runVerification()
            test.runBenchmark()
            println("All SyntaxHighlighter verification checks and benchmarks passed!")
        }
    }

    private val testColors = CodeColors(
        keyword = Color(0xFF0000FF),
        function = Color(0xFF00FF00),
        string = Color(0xFFFF0000),
        number = Color(0xFF00FFFF),
        comment = Color(0xFF888888),
        type = Color(0xFFFF00FF),
        punctuation = Color(0xFF333333),
        plain = Color(0xFF000000),
        gutter = Color(0xFFCCCCCC)
    )

    fun runVerification() {
        testPythonKeywordsHighlighted()
        testRustKeywordsHighlighted()
        testGoKeywordsHighlighted()
        testCommonKeywordsHighlightedInDefaultLanguage()
    }

    fun runBenchmark() {
        val sampleCode = """
            def calculate_fibonacci(n):
                # Calculate nth Fibonacci number
                if n <= 1:
                    return n
                return calculate_fibonacci(n - 1) + calculate_fibonacci(n - 2)
        """.trimIndent()

        // Warm up JIT
        for (i in 0 until 1_000) {
            SyntaxHighlighter.highlight(sampleCode, CodeLanguage.PYTHON, testColors)
        }

        val iterations = 10_000
        val startTime = System.nanoTime()
        for (i in 0 until iterations) {
            SyntaxHighlighter.highlight(sampleCode, CodeLanguage.PYTHON, testColors)
        }
        val elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0
        val perIterationUs = (elapsedMs * 1000.0) / iterations
        println("SyntaxHighlighter benchmark: ${elapsedMs}ms total for $iterations iterations (${perIterationUs}us per highlight)")
        check(perIterationUs < 1000.0) { "Highlighting took too long: ${perIterationUs}us" }
    }

    private fun testPythonKeywordsHighlighted() {
        val code = "def hello():\n    return 42"
        val highlighted = SyntaxHighlighter.highlight(code, CodeLanguage.PYTHON, testColors)

        val defSpan = highlighted.spanStyles.firstOrNull {
            it.start == 0 && it.end == 3
        }
        checkNotNull(defSpan) { "Span for 'def' should exist" }
        check(defSpan.item.color == testColors.keyword) { "Expected keyword color for 'def'" }

        val returnStart = code.indexOf("return")
        val returnSpan = highlighted.spanStyles.firstOrNull {
            it.start == returnStart && it.end == returnStart + "return".length
        }
        checkNotNull(returnSpan) { "Span for 'return' should exist" }
        check(returnSpan.item.color == testColors.keyword) { "Expected keyword color for 'return'" }
    }

    private fun testRustKeywordsHighlighted() {
        val code = "fn main() {\n    let mut x = 10;\n}"
        val highlighted = SyntaxHighlighter.highlight(code, CodeLanguage.RUST, testColors)

        val fnSpan = highlighted.spanStyles.firstOrNull {
            it.start == 0 && it.end == 2
        }
        checkNotNull(fnSpan) { "Span for 'fn' should exist" }
        check(fnSpan.item.color == testColors.keyword) { "Expected keyword color for 'fn'" }

        val mutStart = code.indexOf("mut")
        val mutSpan = highlighted.spanStyles.firstOrNull {
            it.start == mutStart && it.end == mutStart + 3
        }
        checkNotNull(mutSpan) { "Span for 'mut' should exist" }
        check(mutSpan.item.color == testColors.keyword) { "Expected keyword color for 'mut'" }
    }

    private fun testGoKeywordsHighlighted() {
        val code = "func main() {\n    defer recover()\n}"
        val highlighted = SyntaxHighlighter.highlight(code, CodeLanguage.GO, testColors)

        val funcSpan = highlighted.spanStyles.firstOrNull {
            it.start == 0 && it.end == 4
        }
        checkNotNull(funcSpan) { "Span for 'func' should exist" }
        check(funcSpan.item.color == testColors.keyword) { "Expected keyword color for 'func'" }
    }

    private fun testCommonKeywordsHighlightedInDefaultLanguage() {
        val code = "class Foo {\n    if (true) return;\n}"
        val highlighted = SyntaxHighlighter.highlight(code, CodeLanguage.AUTO, testColors)

        val classSpan = highlighted.spanStyles.firstOrNull {
            it.start == 0 && it.end == 5
        }
        checkNotNull(classSpan) { "Span for 'class' should exist" }
        check(classSpan.item.color == testColors.keyword) { "Expected keyword color for 'class'" }
    }
}
