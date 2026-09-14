package com.ai.altercode.ui

import android.text.format.DateUtils

/** "2 hours ago" style formatting for snippet timestamps. */
fun relativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    if (now - timestamp < DateUtils.MINUTE_IN_MILLIS) return "Just now"
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        now,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
}
