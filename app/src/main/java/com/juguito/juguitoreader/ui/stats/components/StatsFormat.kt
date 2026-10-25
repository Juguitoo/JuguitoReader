package com.juguito.juguitoreader.ui.stats.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.StatsPeriod
import com.juguito.juguitoreader.domain.model.ReadingTimeComparison
import com.juguito.juguitoreader.domain.usecase.stats.periodRange
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalLocale

internal enum class ReadingTimeTrend { Up, Down, Flat }

internal data class ReadingTimeTrendUi(
    val trend: ReadingTimeTrend,
    val label: String,
)

@Composable
internal fun statsCardBorder(): BorderStroke =
    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)

@Composable
internal fun statsInsetColor(): Color =
    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)

@Composable
internal fun periodTitle(period: StatsPeriod, anchor: LocalDate): String {
    val locale = LocalLocale.current.platformLocale
    val range = periodRange(period, anchor) ?: return stringResource(R.string.stats_all)
    return when (period) {
        StatsPeriod.WEEK -> {
            val format = DateTimeFormatter.ofPattern("d MMM", locale)
            "${range.start.format(format)} – ${range.endInclusive.format(format)}"
        }
        StatsPeriod.MONTH -> capitalizeMonth(
            anchor.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)),
            locale,
        )
        StatsPeriod.YEAR -> anchor.year.toString()
    }
}

@Composable
internal fun formatReadingMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0 -> stringResource(R.string.session_duration_minutes, minutes)
        minutes == 0 -> stringResource(R.string.session_duration_hours, hours)
        else -> stringResource(R.string.session_duration_hours_minutes, hours, minutes)
    }
}

@Composable
internal fun readingTimeTrend(comparison: ReadingTimeComparison): ReadingTimeTrendUi? {
    val previous = comparison.previousMinutes ?: return null
    if (previous == 0 && comparison.currentMinutes == 0) return null
    if (previous == 0) {
        return ReadingTimeTrendUi(
            trend = ReadingTimeTrend.Up,
            label = stringResource(R.string.stats_vs_previous_none),
        )
    }
    val percent = ((comparison.currentMinutes - previous) * 100.0 / previous).roundToInt()
    return when {
        percent > 0 -> ReadingTimeTrendUi(
            ReadingTimeTrend.Up,
            stringResource(R.string.stats_vs_previous_more, percent),
        )
        percent < 0 -> ReadingTimeTrendUi(
            ReadingTimeTrend.Down,
            stringResource(R.string.stats_vs_previous_less, -percent),
        )
        else -> ReadingTimeTrendUi(
            ReadingTimeTrend.Flat,
            stringResource(R.string.stats_vs_previous_same),
        )
    }
}

internal fun bucketReadout(period: StatsPeriod, start: LocalDate, locale: Locale): String {
    return when (period) {
        StatsPeriod.WEEK -> start.format(DateTimeFormatter.ofPattern("EEEE", locale))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
        StatsPeriod.MONTH -> start.format(DateTimeFormatter.ofPattern("d MMM", locale))
        StatsPeriod.YEAR -> start.format(DateTimeFormatter.ofPattern("LLLL", locale))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }
}

internal fun bucketLabel(period: StatsPeriod, start: LocalDate, locale: Locale): String {
    return when (period) {
        StatsPeriod.WEEK -> start.format(DateTimeFormatter.ofPattern("EEEEE", locale))
        StatsPeriod.MONTH -> start.dayOfMonth.toString()
        StatsPeriod.YEAR -> start.format(DateTimeFormatter.ofPattern("LLL", locale))
    }
}

internal fun monthTitle(month: YearMonth, locale: Locale): String {
    val formatted = month.atDay(1).format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
    return capitalizeMonth(formatted, locale)
}

private fun capitalizeMonth(value: String, locale: Locale): String {
    return value.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}
