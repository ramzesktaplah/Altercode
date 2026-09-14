package com.ai.altercode.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

/** Clipboard, share sheet, and connectivity helpers used across screens. */
object ClipboardTools {

    private const val TAG = "ClipboardTools"
    private const val MAX_PASTE_CHARS = 50_000

    /**
     * Result of a clipboard paste attempt.
     * - [text] is never null when [success] is true.
     * - [truncated] is true when the clipboard content exceeded [MAX_PASTE_CHARS].
     */
    data class PasteResult(val text: String, val truncated: Boolean)

    /**
     * Reads the system clipboard, enforcing MIME type and size limits.
     * Returns null only when the clipboard is empty or not text-based.
     */
    fun paste(context: Context): String? =
        pasteWithMeta(context)?.text

    fun pasteWithMeta(context: Context): PasteResult? {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return null
        return runCatching {
            val clip = manager.primaryClip ?: return null
            if (clip.itemCount == 0) return null
            // MIME type check — skip non-text clips silently.
            if (!clip.description.hasMimeType("text/plain")) return null
            val raw = clip.getItemAt(0).coerceToText(context).toString().takeIf { it.isNotBlank() }
                ?: return null
            val truncated = raw.length > MAX_PASTE_CHARS
            PasteResult(
                text = if (truncated) raw.take(MAX_PASTE_CHARS) else raw,
                truncated = truncated,
            )
        }.getOrElse {
            Log.w(TAG, "Clipboard read failed")
            null
        }
    }

    fun copy(context: Context, label: String, text: String): Boolean {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return false
        return runCatching {
            manager.setPrimaryClip(ClipData.newPlainText(label, text))
            true
        }.getOrElse {
            Log.w(TAG, "Clipboard write failed")
            false
        }
    }

    fun share(context: Context, subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching { context.startActivity(Intent.createChooser(intent, "Share snippet")) }
            .onFailure { Log.w(TAG, "Share sheet unavailable") }
    }

    fun isOnline(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
