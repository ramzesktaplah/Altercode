package com.ai.altercode.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.altercode.ui.theme.MonoFamily
import kotlinx.coroutines.delay

/**
 * True once the launch fade-in has played this session. Provided at the app
 * root so the logo animates only on the first screen of a launch — never
 * again on tab switches or back navigation.
 */
val LocalLogoIntroPlayed = compositionLocalOf { mutableStateOf(false) }

/** The AlterCode brace-and-dots wordmark glyph, `{••}`. Fades in once on launch. */
@Composable
fun AlterCodeMark(
    modifier: Modifier = Modifier,
    braceSize: TextUnit = 28.sp,
    dotSize: Dp = 7.dp,
    showGlow: Boolean = false
) {
    val introPlayed = LocalLogoIntroPlayed.current
    var visible by remember { mutableStateOf(introPlayed.value) }
    LaunchedEffect(Unit) {
        if (!introPlayed.value) {
            // Small beat lets the splash hand off before the mark appears.
            delay(80)
            visible = true
            introPlayed.value = true
        }
    }
    val launchAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 450, easing = LinearEasing),
        label = "logoFadeIn"
    )

    Box(modifier = modifier.alpha(launchAlpha), contentAlignment = Alignment.Center) {
        if (showGlow) {
            Box(
                modifier = Modifier
                    .size(braceSize.value.dp * 3.4f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                            )
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
        color = MaterialTheme.colorScheme.primary,
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
            .background(MaterialTheme.colorScheme.secondary)
    )
}
