package com.juguito.juguitoreader.ui.stats.components

import com.juguito.juguitoreader.domain.model.ReadingStatsBucket

internal const val WORDS_PER_STANDARD_PAGE = 250

internal fun pagesPerHour(wordsPerMinute: Int): Int {
    if (wordsPerMinute <= 0) return 0
    return wordsPerMinute * 60 / WORDS_PER_STANDARD_PAGE
}

internal fun minutesPerStandardPage(wordsPerMinute: Int): Float {
    if (wordsPerMinute <= 0) return 0f
    return WORDS_PER_STANDARD_PAGE.toFloat() / wordsPerMinute
}

internal fun heatIntensity(minutes: Int, peakMinutes: Int): Float {
    if (minutes <= 0 || peakMinutes <= 0) return 0f
    val ratio = (minutes.toFloat() / peakMinutes.toFloat()).coerceIn(0f, 1f)
    return 0.30f + 0.70f * ratio
}

internal fun peakBucketIndex(buckets: List<ReadingStatsBucket>): Int? {
    val index = buckets.indices.maxByOrNull { buckets[it].minutes } ?: return null
    return index.takeIf { buckets[index].minutes > 0 }
}

internal fun chartAxisMax(maxMinutes: Int): Int {
    if (maxMinutes <= 0) return 2
    val step = when {
        maxMinutes <= 10 -> 2
        maxMinutes <= 30 -> 10
        maxMinutes <= 90 -> 30
        maxMinutes <= 240 -> 60
        else -> 120
    }
    return ((maxMinutes + step - 1) / step) * step
}
