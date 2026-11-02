package com.ai.altercode.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.altercode.data.CodeLanguage
import com.ai.altercode.ui.theme.MonoFamily

/**
 * Brand-styled language logo mark — the Kotlin gradient flag, the TS tile,
 * Python's two-snake interlock, Rust's seal, and so on — drawn with plain
 * geometry so no image assets are needed.
 */
@Composable
fun LanguageLogo(language: CodeLanguage, size: Dp, modifier: Modifier = Modifier) {
    when (language) {
        CodeLanguage.KOTLIN -> KotlinMark(size, modifier)
        CodeLanguage.PYTHON -> PythonMark(size, modifier)
        CodeLanguage.RUST -> CircleMonogram(size, Color(0xFFA83226), "R", modifier)
        CodeLanguage.TYPESCRIPT -> MonogramTile(size, Color(0xFF3178C6), "TS", Color.White, modifier)
        CodeLanguage.JAVASCRIPT -> MonogramTile(size, Color(0xFFF7DF1E), "JS", Color(0xFF1A1A1A), modifier)
        CodeLanguage.GO -> MonogramTile(size, Color(0xFF00ADD8), "GO", Color.White, modifier)
        CodeLanguage.CSHARP -> MonogramTile(size, Color(0xFF68217A), "C#", Color.White, modifier, fontScale = 0.34f)
        CodeLanguage.JAVA -> JavaCupMark(size, modifier)
        CodeLanguage.CPP -> HexMonogram(size, Color(0xFF00599C), "C++", modifier)
        CodeLanguage.C -> MonogramTile(size, Color(0xFF03599C), "C", Color.White, modifier)
        CodeLanguage.SWIFT -> MonogramTile(size, Color(0xFFF05138), "SW", Color.White, modifier)
        CodeLanguage.DART -> DartMark(size, modifier)
        CodeLanguage.RUBY -> RubyMark(size, modifier)
        CodeLanguage.PHP -> PillMonogram(size, Color(0xFF777BB4), "php", modifier)
        CodeLanguage.OBJECTIVE_C -> MonogramTile(size, Color(0xFF1B6AC9), "OC", Color.White, modifier)
        CodeLanguage.R_LANG -> MonogramTile(size, Color(0xFF276DC3), "R", Color.White, modifier)
        CodeLanguage.PERL -> MonogramTile(size, Color(0xFF39457E), "PL", Color.White, modifier)
        CodeLanguage.LUA -> LuaMark(size, modifier)
        CodeLanguage.HASKELL -> MonogramTile(size, Color(0xFF5D4F85), "λ", Color.White, modifier)
        else -> MonogramTile(size, language.accent, language.badge, Color.White, modifier)
    }
}

/** Brand-colored rounded square with a monogram — the TS / GO / R style mark. */
@Composable
private fun MonogramTile(
    size: Dp,
    background: Color,
    text: String,
    textColor: Color,
    modifier: Modifier = Modifier,
    fontScale: Float = 0.42f
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.2f))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * fontScale).sp,
            maxLines = 1
        )
    }
}

@Composable
private fun CircleMonogram(size: Dp, background: Color, text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.46f).sp,
            maxLines = 1
        )
    }
}

@Composable
private fun PillMonogram(size: Dp, background: Color, text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(size)
            .height(size * 0.72f)
            .clip(RoundedCornerShape(50))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.3f).sp,
            maxLines = 1
        )
    }
}

/** Kotlin's diagonal flag with the official purple→pink gradient. */
@Composable
private fun KotlinMark(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.18f))
    ) {
        Canvas(Modifier.matchParentSize()) {
            val w = this.size.width
            val h = this.size.height
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w * 0.5f, h * 0.5f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path,
                Brush.linearGradient(
                    colors = listOf(Color(0xFFE44857), Color(0xFFC711E1), Color(0xFF7F52FF))
                )
            )
        }
    }
}

/** Python's two interlocking snakes in the official blue and yellow. */
@Composable
private fun PythonMark(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val r = CornerRadius(w * 0.16f)
        val blue = Color(0xFF3776AB)
        val yellow = Color(0xFFFFD43B)

        // Top snake: head block + neck curving down on the left.
        drawRoundRect(blue, Offset(w * 0.06f, 0f), Size(w * 0.88f, h * 0.52f), r)
        drawRoundRect(blue, Offset(w * 0.06f, h * 0.16f), Size(w * 0.5f, h * 0.48f), r)
        drawCircle(Color(0xFF1E415B), radius = w * 0.06f, center = Offset(w * 0.72f, h * 0.17f))

        // Bottom snake mirrored: head block + neck curving up on the right.
        drawRoundRect(yellow, Offset(w * 0.06f, h * 0.48f), Size(w * 0.88f, h * 0.52f), r)
        drawRoundRect(yellow, Offset(w * 0.44f, h * 0.36f), Size(w * 0.5f, h * 0.48f), r)
        drawCircle(Color(0xFF5B431B), radius = w * 0.06f, center = Offset(w * 0.28f, h * 0.83f))
    }
}

/** Dart's two-tone kite blade. */
@Composable
private fun DartMark(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val light = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w, h * 0.5f)
            lineTo(0f, h * 0.5f)
            close()
        }
        val dark = Path().apply {
            moveTo(0f, h * 0.5f)
            lineTo(w, h * 0.5f)
            lineTo(w * 0.5f, h)
            close()
        }
        drawPath(light, Color(0xFF47C5FB))
        drawPath(dark, Color(0xFF015B8B))
    }
}

/** Ruby's faceted gem. */
@Composable
private fun RubyMark(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val gem = Path().apply {
            moveTo(w * 0.18f, h * 0.08f)
            lineTo(w * 0.82f, h * 0.08f)
            lineTo(w * 0.97f, h * 0.38f)
            lineTo(w * 0.5f, h * 0.95f)
            lineTo(w * 0.03f, h * 0.38f)
            close()
        }
        drawPath(gem, Color(0xFFCC342D))
        val facet = Stroke(width = w * 0.035f)
        val line = Path().apply {
            moveTo(w * 0.03f, h * 0.38f)
            lineTo(w * 0.97f, h * 0.38f)
        }
        drawPath(line, Color.White.copy(alpha = 0.35f), style = facet)
        val facetLeft = Path().apply {
            moveTo(w * 0.18f, h * 0.08f)
            lineTo(w * 0.35f, h * 0.38f)
            lineTo(w * 0.5f, h * 0.95f)
        }
        drawPath(facetLeft, Color.White.copy(alpha = 0.35f), style = facet)
    }
}

/** Lua's blue moon with its satellite. */
@Composable
private fun LuaMark(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val blue = Color(0xFF2D79C7)
        drawCircle(blue, radius = w * 0.38f, center = Offset(w * 0.44f, h * 0.56f))
        drawCircle(Color.White, radius = w * 0.14f, center = Offset(w * 0.58f, h * 0.42f))
        drawCircle(blue, radius = w * 0.1f, center = Offset(w * 0.85f, h * 0.15f))
    }
}

/** Java's coffee cup silhouette in the official orange with blue steam. */
@Composable
private fun JavaCupMark(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val orange = Color(0xFFE76F00)
        val blue = Color(0xFF5382A1)

        // Cup body.
        drawRoundRect(
            orange,
            Offset(w * 0.28f, h * 0.42f),
            Size(w * 0.42f, h * 0.4f),
            CornerRadius(w * 0.08f)
        )
        // Handle.
        drawArc(
            color = orange,
            startAngle = -80f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(w * 0.62f, h * 0.48f),
            size = Size(w * 0.28f, h * 0.28f),
            style = Stroke(width = w * 0.07f)
        )
        // Steam.
        drawCircle(blue, radius = w * 0.045f, center = Offset(w * 0.4f, h * 0.28f))
        drawCircle(blue, radius = w * 0.045f, center = Offset(w * 0.56f, h * 0.18f))
    }
}

/** C++'s blue hexagon with a monogram. */
@Composable
private fun HexMonogram(size: Dp, color: Color, text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val w = this.size.width
            val h = this.size.height
            val path = Path().apply {
                moveTo(w * 0.5f, 0f)
                lineTo(w, h * 0.25f)
                lineTo(w, h * 0.75f)
                lineTo(w * 0.5f, h)
                lineTo(0f, h * 0.75f)
                lineTo(0f, h * 0.25f)
                close()
            }
            drawPath(path, color)
        }
        Text(
            text = text,
            color = Color.White,
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.3f).sp,
            maxLines = 1
        )
    }
}

/** Language tile plus label, used in lists and metadata rows. */
@Composable
fun LanguageBadge(
    language: CodeLanguage,
    modifier: Modifier = Modifier,
    tileSize: Int = 20,
    showLabel: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LanguageLogo(language = language, size = tileSize.dp)
        if (showLabel) {
            Text(
                text = language.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = 2.dp),
                maxLines = 1
            )
        }
    }
}
