package com.juguito.juguitoreader.domain.usecase.stats

import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.enums.StatsPeriod
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.model.ReadingGenreStat
import com.juguito.juguitoreader.domain.model.ReadingStats
import com.juguito.juguitoreader.domain.model.ReadingStatsBucket
import com.juguito.juguitoreader.domain.model.ReadingTimeComparison
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil

fun aggregateReadingStats(
    books: List<Book>,
    dailyReadings: List<DailyReading>,
    period: StatsPeriod,
    today: LocalDate,
    streakAsOf: LocalDate = today,
): ReadingStats {
    val periodRange = periodRange(period, today)

    val totalFinished = books.filter { it.status == BookStatus.FINISHED }
    val finishedInPeriod = if (periodRange != null) {
        totalFinished.filter { it.endDate != null && periodRange.contains(it.endDate.asUtcDate()) }
    } else {
        totalFinished
    }

    val readingDays = readingDays(dailyReadings)
    val dailyReadingsInPeriod = if (periodRange != null) {
        dailyReadingsInPeriod(dailyReadings, periodRange)
    } else {
        dailyReadings
    }
    val minutesByDay = minutesByDay(dailyReadingsInPeriod)

    return ReadingStats(
        totalFinished = totalFinished.size,
        finishedInPeriod = finishedInPeriod.sortedByDescending { it.endDate ?: Long.MIN_VALUE },
        readingStreak = currentStreak(readingDays, streakAsOf),
        bestReadingStreak = bestCurrentStreak(readingDays),
        readingMinutes = readingMinutes(minutesByDay, periodRange, period),
        readingTimeComparison = ReadingTimeComparison(
            currentMinutes = minutesByDay.values.sum(),
            previousMinutes = previousSliceMinutes(dailyReadings, today, period)
        ),
        daysReadInPeriod = readingDays(dailyReadingsInPeriod).size,
        readingAverageVelocity = readingAverageVelocity(dailyReadingsInPeriod),
        averageFinishedRating = averageFinishedRating(totalFinished),
        mostAllReadGenre = mostReadGenre(totalFinished),
        mostReadGenreInPeriod = mostReadGenre(finishedInPeriod)
    )
}

private fun Long.asUtcDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

internal fun periodRange(period: StatsPeriod, today: LocalDate): ClosedRange<LocalDate>? {
    return when (period) {
        StatsPeriod.WEEK -> {
            val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            start..start.plusDays(6)
        }
        StatsPeriod.MONTH -> {
            val start = today.withDayOfMonth(1)
            start..start.plusDays(today.lengthOfMonth().toLong() - 1)
        }
        StatsPeriod.YEAR -> {
            val start = today.withDayOfYear(1)
            start..start.with(TemporalAdjusters.lastDayOfYear())
        }
    }
}

private fun readingDays(dailyReadings: List<DailyReading>): Set<LocalDate> =
    dailyReadings.filter { it.timeSpentMillis > 0 }
        .map { LocalDate.parse(it.date) }
        .toSet()

private fun dailyReadingsInPeriod(
    dailyReadings: List<DailyReading>,
    periodRange: ClosedRange<LocalDate>
): List<DailyReading> {
    return dailyReadings.filter { LocalDate.parse(it.date) in periodRange }
}

private fun currentStreak(days: Set<LocalDate>, today: LocalDate): Int {
    var cursor = if (today in days) today else today.minusDays(1)
    if (cursor !in days) return 0
    var streak = 0
    while (cursor in days) {
        streak++
        cursor = cursor.minusDays(1)
    }
    return streak
}

private fun bestCurrentStreak(days: Set<LocalDate>): Int {
    if (days.isEmpty()) return 0
    val sorted = days.sorted()
    var best = 1
    var current = 1
    for (i in 1 until sorted.size) {
        current = if (sorted[i] == sorted[i - 1].plusDays(1)) current + 1 else 1
        if (current > best) best = current
    }
    return best
}

private fun previousSliceMinutes(
    readings: List<DailyReading>,
    today: LocalDate,
    period: StatsPeriod
): Int? {
    val current = periodRange(period, today) ?: return null
    val elapsed = ChronoUnit.DAYS.between(current.start, today) + 1
    val previousStart = when (period) {
        StatsPeriod.WEEK -> current.start.minusWeeks(1)
        StatsPeriod.MONTH -> current.start.minusMonths(1)
        StatsPeriod.YEAR -> current.start.minusYears(1)
    }
    val previousEnd = when (period) {
        StatsPeriod.WEEK -> previousStart.plusDays(6)
        StatsPeriod.MONTH -> previousStart.withDayOfMonth(previousStart.lengthOfMonth())
        StatsPeriod.YEAR -> previousStart.with(TemporalAdjusters.lastDayOfYear())
    }
    val sliceEnd = minOf(previousStart.plusDays(elapsed - 1), previousEnd)
    val slice = previousStart..sliceEnd
    return minutesByDay(dailyReadingsInPeriod(readings, slice)).values.sum()
}

private fun minutesByDay(readings: List<DailyReading>): Map<LocalDate, Int> {
    return readings.groupBy { LocalDate.parse(it.date) }.mapValues { (_, day) ->
        val millis = day.sumOf { it.timeSpentMillis.toLong() }
        ceil(millis / 60_000.0).toInt()
    }
}

private fun readingMinutes(
    minutesByDay: Map<LocalDate, Int>,
    periodRange: ClosedRange<LocalDate>?,
    period: StatsPeriod
): List<ReadingStatsBucket> {
    if (periodRange == null) return emptyList()

    val buckets = mutableListOf<ReadingStatsBucket>()
    if (period == StatsPeriod.WEEK || period == StatsPeriod.MONTH) {
        var day = periodRange.start
        while (day in periodRange) {
            buckets += ReadingStatsBucket(day, minutesByDay[day] ?: 0)
            day = day.plusDays(1)
        }
    } else if (period == StatsPeriod.YEAR) {
        var month = periodRange.start.withDayOfMonth(1)
        repeat(12) {
            val minutes = minutesByDay
                .filterKeys { it.year == month.year && it.month == month.month }
                .values.sum()
            buckets += ReadingStatsBucket(month, minutes)
            month = month.plusMonths(1)
        }
    }
    return buckets
}

private fun readingAverageVelocity(dailyReadings: List<DailyReading>): Int {
    val counted = dailyReadings.filter { it.timeSpentMillis > 0 }
    if (counted.isEmpty()) return 0
    val sumVelocity = counted.sumOf { it.readingSpeed.toLong() * it.timeSpentMillis.toLong() }
    val sumTimes = counted.sumOf { it.timeSpentMillis.toLong() }
    return (sumVelocity / sumTimes).toInt()
}

private fun averageFinishedRating(finished: List<Book>): Float? {
    val rated = finished.filter { it.rating > 0f }
    if (rated.isEmpty()) return null
    return (rated.sumOf { it.rating.toDouble() } / rated.size).toFloat()
}

private fun mostReadGenre(finished: List<Book>): ReadingGenreStat? {
    val groups = finished
        .flatMap { book -> book.genres.distinctBy { it.id } }
        .groupBy { it.id }
    val winner = groups.values
        .sortedWith(
            compareByDescending<List<Genre>> { it.size }
                .thenBy { it.first().name.lowercase() }
        )
        .firstOrNull()
        ?: return null
    return ReadingGenreStat(winner.first(), winner.size)
}
