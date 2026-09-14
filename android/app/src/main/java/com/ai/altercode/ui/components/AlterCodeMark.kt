package com.ai.altercode.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.altercode.ui.theme.AccentBlue
import com.ai.altercode.ui.theme.AccentBlueSoft
import com.ai.altercode.ui.theme.MonoFamily

/** The AlterCode brace-and-dots wordmark glyph, `{••}`. */
@Composable
fun AlterCodeMark(
    modifier: Modifier = Modifier,
    braceSize: TextUnit = 28.sp,
    dotSize: Dp = 7.dp,
    showGlow: Boolean = false
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (showGlow) {
            Box(
                modifier = Modifier
                    .size(braceSize.value.dp * 3.4f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(AccentBlue.copy(alpha = 0.22f), AccentBlue.copy(alpha = 0f))
                        )
                    )
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Brace(text = "{", size = braceSize)
            Row(
                horizontalArrangement = Arrangement.spacedBy(dotSize * 0.55f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Dot(dotSize)
                Dot(dotSize)
            }
            Brace(text = "}", size = braceSize)
        }
    }
}

@Composable
private fun Brace(text: String, size: TextUnit) {
    Text(
        text = text,
        color = AccentBlue,
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = size,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun Dot(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(AccentBlueSoft)
    )
}
