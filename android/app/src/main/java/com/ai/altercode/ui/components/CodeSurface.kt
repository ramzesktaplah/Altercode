package com.ai.altercode.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.altercode.data.CodeLanguage
import com.ai.altercode.ui.theme.LocalCodeColors
import com.ai.altercode.ui.theme.MonoFamily
import com.ai.altercode.ui.theme.SyntaxHighlighter

private val codeTextStyle = TextStyle(
    fontFamily = MonoFamily,
    fontSize = 13.sp,
    lineHeight = 22.sp
)

/** Read-only, syntax-highlighted code view with a line-number gutter. */
@Composable
fun CodeBlock(
    code: String,
    language: CodeLanguage,
    modifier: Modifier = Modifier
) {
    val codeColors = LocalCodeColors.current
    val highlighted = remember(code, language, codeColors) {
        SyntaxHighlighter.highlight(code, language, codeColors)
    }
    val lineCount = remember(code) { code.lines().size }

    Row(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 14.dp)
    ) {
        LineGutter(lineCount = lineCount)
        Text(
            text = highlighted,
            style = codeTextStyle,
            softWrap = false,
            modifier = Modifier
                .defaultMinSize(minWidth = 220.dp)
                .padding(end = 20.dp)
        )
    }
}

/** Plain-prose viewer used for AI explanations. */
@Composable
fun ProseBlock(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        text.split("\n").forEach { line ->
            if (line.isBlank()) return@forEach
            val cleaned = line.replace("**", "").replace("`", "").trimEnd()
            val isHeading = cleaned.endsWith(":") && cleaned.length < 60
            Text(
                text = cleaned,
                style = if (isHeading) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                color = if (isHeading) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/** Editable code field with gutter, monospace type, and live highlighting. */
@Composable
fun CodeEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    language: CodeLanguage,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val codeColors = LocalCodeColors.current
    val lineCount = remember(value.text) { value.text.lines().size }
    val transformation = remember(language, codeColors) {
        VisualTransformation { original ->
            androidx.compose.ui.text.input.TransformedText(
                SyntaxHighlighter.highlight(original.text, language, codeColors),
                androidx.compose.ui.text.input.OffsetMapping.Identity
            )
        }
    }

    Row(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 14.dp)
    ) {
        LineGutter(lineCount = lineCount)
        Box {
            if (value.text.isEmpty()) {
                Text(
                    text = "Paste or type your code here…",
                    style = codeTextStyle,
                    color = codeColors.comment
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                textStyle = codeTextStyle.copy(color = codeColors.plain),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = transformation,
                modifier = Modifier
                    .defaultMinSize(minWidth = 240.dp)
                    .padding(end = 20.dp)
            )
        }
    }
}

@Composable
private fun LineGutter(lineCount: Int) {
    val codeColors = LocalCodeColors.current
    Column(
        modifier = Modifier
            .width(if (lineCount >= 100) 42.dp else 32.dp)
            .padding(end = 12.dp)
    ) {
        for (line in 1..lineCount) {
            Text(
                text = line.toString(),
                style = codeTextStyle,
                color = codeColors.gutter,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Convenience wrapper to render an annotated string in the shared code style. */
@Composable
fun MonoText(text: AnnotatedString, modifier: Modifier = Modifier) {
    Text(text = text, style = codeTextStyle, modifier = modifier)
}

/** Fills its parent with the shared code text style (used for measuring/preview). */
@Composable
fun CodeStyleProvider(content: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalTextStyle provides codeTextStyle,
        content = content
    )
}

/** Empty helper kept for layout composition in screens. */
@Composable
fun FillBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize()) { content() }
}
