package com.juguito.juguitoreader.utils

import com.juguito.juguitoreader.domain.model.DailyReading

private const val FALLBACK_WPM = 250
private const val MIN_HISTORY_MILLIS = 5 * 60_000L

fun resolveWpm(sessions: List<DailyReading>): Int {
    val totalMillis = sessions.sumOf { it.timeSpentMillis.toLong() }
    if (totalMillis < MIN_HISTORY_MILLIS) return FALLBACK_WPM

    val weighted = sessions.sumOf { it.readingSpeed.toLong() * it.timeSpentMillis.toLong() }
    return (weighted / totalMillis).toInt().coerceAtLeast(1)
}
