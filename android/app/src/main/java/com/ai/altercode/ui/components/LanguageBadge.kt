package com.ai.altercode.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.altercode.data.CodeLanguage
import com.ai.altercode.ui.theme.MonoFamily

/** Square language chip, e.g. the blue `TS` tile. */
@Composable
fun LanguageTile(language: CodeLanguage, modifier: Modifier = Modifier, size: Int = 20) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 4).dp))
            .background(language.accent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = language.badge,
            color = Color.White,
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.44f).sp,
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
        LanguageTile(language = language, size = tileSize)
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
