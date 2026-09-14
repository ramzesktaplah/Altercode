package com.ai.altercode.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ai.altercode.data.ClipboardTools
import com.ai.altercode.data.CodeAction
import com.ai.altercode.ui.components.CodeBlock
import com.ai.altercode.ui.components.LanguageBadge
import com.ai.altercode.ui.components.ProseBlock
import com.ai.altercode.ui.relativeTime
import kotlinx.coroutines.launch

/** Result view for a single snippet: metadata, output, and one-tap utilities. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetDetailScreen(
    snippetId: Long,
    onBack: () -> Unit,
    onRunAgain: () -> Unit,
    viewModel: SnippetDetailViewModel = viewModel()
) {
    val snippets by viewModel.snippets.collectAsStateWithLifecycle()
    val snippet = remember(snippets, snippetId) { snippets.firstOrNull { it.id == snippetId } }
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSource by remember { mutableStateOf(false) }

    if (snippet == null) {
        MissingSnippet(onBack = onBack)
        return
    }

    fun copyResult() {
        val copied = ClipboardTools.copy(context, snippet.title, snippet.result)
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            snackbarHostState.showSnackbar(
                if (copied) "Copied to clipboard" else "Couldn't access the clipboard"
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (snippet.action == CodeAction.EXPLAIN) "Explanation" else "Snippet",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(snippet.id) }) {
                        Icon(
                            imageVector = if (snippet.isFavorite) {
                                Icons.Filled.Star
                            } else {
                                Icons.Filled.StarBorder
                            },
                            contentDescription = "Favorite",
                            tint = if (snippet.isFavorite) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    IconButton(
                        onClick = {
                            ClipboardTools.share(context, snippet.title, snippet.result)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { copyResult() }) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = snippet.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LanguageBadge(language = snippet.sourceLanguage, tileSize = 22)
                    val target = snippet.targetLanguage
                    if (target != null) {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "to",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .size(16.dp)
                        )
                        LanguageBadge(language = target, tileSize = 22)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "  ${snippet.action.pastLabel} • ${relativeTime(snippet.createdAt)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (snippet.summary.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = snippet.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                if (snippet.action.producesCode) {
                    CodeBlock(
                        code = snippet.result,
                        language = snippet.resultLanguage,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp)
                    )
                } else {
                    ProseBlock(text = snippet.result, modifier = Modifier.fillMaxSize())
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSource = !showSource }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showSource) "Hide original code" else "Show original code",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (showSource) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(18.dp)
                )
            }

            AnimatedVisibility(visible = showSource) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    CodeBlock(
                        code = snippet.sourceCode,
                        language = snippet.sourceLanguage,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailAction(
                    label = "Copy",
                    icon = Icons.Filled.ContentCopy,
                    modifier = Modifier.weight(1f),
                    onClick = { copyResult() }
                )
                DetailAction(
                    label = "Share",
                    icon = Icons.Filled.Share,
                    modifier = Modifier.weight(1f),
                    onClick = { ClipboardTools.share(context, snippet.title, snippet.result) }
                )
                DetailAction(
                    label = "Run Again",
                    icon = Icons.Filled.PlayArrow,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.runAgain(snippet)
                        onRunAgain()
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(17.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MissingSnippet(onBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Snippet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "This snippet is no longer available.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
