package com.ai.altercode.ui.theme

import androidx.compose.ui.graphics.Color
import com.ai.altercode.data.CodeLanguage

fun main() {
    val testColors = CodeColors(
        keyword = Color.Red,
        string = Color.Green,
        number = Color.Blue,
        comment = Color.Gray,
        function = Color.Yellow,
        type = Color.Magenta,
        plain = Color.White,
        punctuation = Color.DarkGray,
        gutter = Color.LightGray
    )

    val pyCode = "def hello():\n    # python comment\n    return \"world\""
    val pyHighlighted = SyntaxHighlighter.highlight(pyCode, CodeLanguage.PYTHON, testColors)
    check(pyHighlighted.text == pyCode) { "Python highlighting text mismatch" }

    val ktCode = "fun main() {\n    // kotlin comment\n    println(123)\n}"
    val ktHighlighted = SyntaxHighlighter.highlight(ktCode, CodeLanguage.KOTLIN, testColors)
    check(ktHighlighted.text == ktCode) { "Kotlin highlighting text mismatch" }

    val rsCode = "fn main() {\n    let mut x = 42;\n    // rust comment\n}"
    val rsHighlighted = SyntaxHighlighter.highlight(rsCode, CodeLanguage.RUST, testColors)
    check(rsHighlighted.text == rsCode) { "Rust highlighting text mismatch" }

    println("All SyntaxHighlighter checks passed successfully!")
}
