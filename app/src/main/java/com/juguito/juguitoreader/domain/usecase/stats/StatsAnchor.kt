package com.juguito.juguitoreader.domain.usecase.stats

import com.juguito.juguitoreader.domain.enums.StatsPeriod
import java.time.LocalDate

internal fun previousStatsAnchor(period: StatsPeriod, anchor: LocalDate): LocalDate? {
    val range = periodRange(period, anchor) ?: return null
    return range.start.minusDays(1)
}

internal fun nextStatsAnchor(period: StatsPeriod, anchor: LocalDate, today: LocalDate): LocalDate? {
    val range = periodRange(period, anchor) ?: return null
    if (!range.endInclusive.isBefore(today)) return null
    val nextRange = periodRange(period, range.endInclusive.plusDays(1)) ?: return null
    return if (nextRange.endInclusive.isBefore(today)) nextRange.endInclusive else today
}
