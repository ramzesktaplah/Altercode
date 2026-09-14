package com.ai.altercode.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ai.altercode.R

val CanvasSlate = Color(0xFF121826)
val SurfaceRaised = Color(0xFF1A2233)
val SurfaceCard = Color(0xFF1E293B)
val SurfaceStroke = Color(0xFF2B3648)
val AccentBlue = Color(0xFF3B82F6)
val AccentBlueSoft = Color(0xFF60A5FA)
val SuccessGreen = Color(0xFF34C759)
val DangerRed = Color(0xFFF87171)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

val MonoFamily: FontFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
)

/** Token set for syntax highlighting so code rendering stays consistent app-wide. */
@Immutable
data class CodeColors(
    val plain: Color = Color(0xFFE2E8F0),
    val keyword: Color = Color(0xFFC792EA),
    val function: Color = Color(0xFF82AAFF),
    val string: Color = Color(0xFFC3E88D),
    val number: Color = Color(0xFFF78C6C),
    val comment: Color = Color(0xFF5A6B87),
    val type: Color = Color(0xFFFFCB6B),
    val punctuation: Color = Color(0xFF8BA3C7),
    val gutter: Color = Color(0xFF475569)
)

val LocalCodeColors = staticCompositionLocalOf { CodeColors() }

private val AlterCodeColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1D3A6B),
    onPrimaryContainer = Color(0xFFD9E7FF),
    secondary = AccentBlueSoft,
    onSecondary = Color(0xFF06122A),
    secondaryContainer = Color(0xFF24344F),
    onSecondaryContainer = Color(0xFFDCE7F7),
    tertiary = SuccessGreen,
    onTertiary = Color(0xFF04210C),
    background = CanvasSlate,
    onBackground = TextPrimary,
    surface = CanvasSlate,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = Color(0xFF0D1320),
    surfaceContainerLow = Color(0xFF151D2C),
    surfaceContainer = SurfaceRaised,
    surfaceContainerHigh = SurfaceCard,
    surfaceContainerHighest = Color(0xFF243044),
    outline = SurfaceStroke,
    outlineVariant = Color(0xFF222C3D),
    error = DangerRed,
    onError = Color(0xFF2A0A0A),
    errorContainer = Color(0xFF3A1717),
    onErrorContainer = Color(0xFFFFD9D9),
    scrim = Color(0xFF04070E)
)

private val AlterCodeTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    )
)

/** AlterCode is a dark-only, OLED-friendly developer surface. */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AlterCodeColorScheme,
        typography = AlterCodeTypography,
        content = content
    )
}
