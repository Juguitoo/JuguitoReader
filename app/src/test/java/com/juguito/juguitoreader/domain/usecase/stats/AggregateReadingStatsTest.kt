package com.juguito.juguitoreader.domain.usecase.stats

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.enums.StatsPeriod
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.model.Genre
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class AggregateReadingStatsTest {

    private val wednesday = LocalDate.of(2026, 10, 14)

    @Test
    fun `week keeps monday through sunday and drops the sunday before`() {
        val stats = aggregateReadingStats(
            books = listOf(
                finished(1, LocalDate.of(2026, 10, 12)),
                finished(2, LocalDate.of(2026, 10, 18)),
                finished(3, LocalDate.of(2026, 10, 11)),
            ),
            dailyReadings = emptyList(),
            period = StatsPeriod.WEEK,
            today = wednesday,
        )

        assertThat(stats.finishedInPeriod.map { it.id }).containsExactly(2, 1).inOrder()
        assertThat(stats.totalFinished).isEqualTo(3)
    }

    @Test
    fun `month and year keep readings inside the calendar period`() {
        val readings = listOf(
            session(1, LocalDate.of(2026, 9, 30)),
            session(1, LocalDate.of(2026, 10, 1)),
            session(1, LocalDate.of(2025, 12, 31)),
            session(1, LocalDate.of(2026, 1, 1)),
        )

        val month = aggregateReadingStats(emptyList(), readings, StatsPeriod.MONTH, wednesday)
        val year = aggregateReadingStats(emptyList(), readings, StatsPeriod.YEAR, wednesday)

        assertThat(month.daysReadInPeriod).isEqualTo(1)
        assertThat(month.readingTimeComparison.currentMinutes).isEqualTo(1)
        assertThat(year.daysReadInPeriod).isEqualTo(3)
        assertThat(year.readingTimeComparison.currentMinutes).isEqualTo(3)
    }

    @Test
    fun `finished without an end date counts in the total and not in the month`() {
        val stats = aggregateReadingStats(
            books = listOf(
                finished(1, LocalDate.of(2026, 10, 2)),
                finished(2, LocalDate.of(2026, 9, 2)),
                finished(3, null),
                dropped(4, LocalDate.of(2026, 10, 3)),
            ),
            dailyReadings = emptyList(),
            period = StatsPeriod.MONTH,
            today = wednesday,
        )

        assertThat(stats.totalFinished).isEqualTo(3)
        assertThat(stats.finishedInPeriod.map { it.id }).containsExactly(1)
    }

    @Test
    fun `physical finished book is listed and does not add minutes`() {
        val stats = aggregateReadingStats(
            books = listOf(finished(1, LocalDate.of(2026, 10, 10), physical = true)),
            dailyReadings = emptyList(),
            period = StatsPeriod.MONTH,
            today = wednesday,
        )

        assertThat(stats.finishedInPeriod.map { it.id }).containsExactly(1)
        assertThat(stats.readingTimeComparison.currentMinutes).isEqualTo(0)
        assertThat(stats.readingAverageVelocity).isEqualTo(0)
    }

    @Test
    fun `velocity weights by time and ignores a day with no time`() {
        val stats = aggregateReadingStats(
            books = emptyList(),
            dailyReadings = listOf(
                session(1, LocalDate.of(2026, 10, 1), millis = 120_000, speed = 200),
                session(1, LocalDate.of(2026, 10, 2), millis = 60_000, speed = 100),
                session(1, LocalDate.of(2026, 10, 3), millis = 0, speed = 500),
            ),
            period = StatsPeriod.MONTH,
            today = wednesday,
        )

        assertThat(stats.readingAverageVelocity).isEqualTo(166)
        assertThat(stats.daysReadInPeriod).isEqualTo(2)
    }

    @Test
    fun `minutes round each day up and sum two sessions of the same day once`() {
        val stats = aggregateReadingStats(
            books = emptyList(),
            dailyReadings = listOf(
                session(1, LocalDate.of(2026, 10, 12), millis = 30_000),
                session(2, LocalDate.of(2026, 10, 12), millis = 30_000),
                session(1, LocalDate.of(2026, 10, 13), millis = 1),
            ),
            period = StatsPeriod.WEEK,
            today = wednesday,
        )

        assertThat(stats.readingMinutes).hasSize(7)
        assertThat(stats.readingMinutes.first().start).isEqualTo(LocalDate.of(2026, 10, 12))
        assertThat(stats.readingMinutes.map { it.minutes }).containsExactly(1, 1, 0, 0, 0, 0, 0).inOrder()
        assertThat(stats.readingTimeComparison.currentMinutes).isEqualTo(2)
    }

    @Test
    fun `month has one bucket per day and a non leap february has 28`() {
        val october = aggregateReadingStats(emptyList(), emptyList(), StatsPeriod.MONTH, wednesday)
        val february = aggregateReadingStats(
            emptyList(),
            emptyList(),
            StatsPeriod.MONTH,
            LocalDate.of(2026, 2, 10),
        )

        assertThat(october.readingMinutes).hasSize(31)
        assertThat(february.readingMinutes).hasSize(28)
        assertThat(february.readingMinutes.last().start).isEqualTo(LocalDate.of(2026, 2, 28))
    }

    @Test
    fun `year has twelve monthly buckets`() {
        val stats = aggregateReadingStats(
            books = emptyList(),
            dailyReadings = listOf(
                session(1, LocalDate.of(2026, 1, 3), millis = 60_000),
                session(1, LocalDate.of(2026, 1, 4), millis = 60_000),
            ),
            period = StatsPeriod.YEAR,
            today = wednesday,
        )

        assertThat(stats.readingMinutes).hasSize(12)
        assertThat(stats.readingMinutes.first().start).isEqualTo(LocalDate.of(2026, 1, 1))
        assertThat(stats.readingMinutes.first().minutes).isEqualTo(2)
        assertThat(stats.readingMinutes[1].minutes).isEqualTo(0)
    }

    @Test
    fun `previous month slice stops on the same day number`() {
        val stats = aggregateReadingStats(
            books = emptyList(),
            dailyReadings = listOf(
                session(1, LocalDate.of(2026, 9, 14)),
                session(1, LocalDate.of(2026, 9, 15)),
                session(1, LocalDate.of(2026, 10, 14)),
            ),
            period = StatsPeriod.MONTH,
            today = wednesday,
        )

        assertThat(stats.readingTimeComparison.currentMinutes).isEqualTo(1)
        assertThat(stats.readingTimeComparison.previousMinutes).isEqualTo(1)
    }

    @Test
    fun `previous week slice stops on the same weekday`() {
        val stats = aggregateReadingStats(
            books = emptyList(),
            dailyReadings = listOf(
                session(1, LocalDate.of(2026, 10, 5)),
                session(1, LocalDate.of(2026, 10, 7)),
                session(1, LocalDate.of(2026, 10, 8)),
            ),
            period = StatsPeriod.WEEK,
            today = wednesday,
        )

        assertThat(stats.readingTimeComparison.previousMinutes).isEqualTo(2)
    }

    @Test
    fun `shorter previous month is cut on its last day`() {
        val stats = aggregateReadingStats(
            books = emptyList(),
            dailyReadings = listOf(session(1, LocalDate.of(2026, 2, 28))),
            period = StatsPeriod.MONTH,
            today = LocalDate.of(2026, 3, 31),
        )

        assertThat(stats.readingTimeComparison.previousMinutes).isEqualTo(1)
    }

    @Test
    fun `streak stays alive through yesterday and the record is the longest run`() {
        val days = listOf(
            LocalDate.of(2026, 10, 1),
            LocalDate.of(2026, 10, 2),
            LocalDate.of(2026, 10, 3),
            LocalDate.of(2026, 10, 4),
            LocalDate.of(2026, 10, 11),
            LocalDate.of(2026, 10, 12),
            LocalDate.of(2026, 10, 13),
        )
        val stats = aggregateReadingStats(
            books = emptyList(),
            dailyReadings = days.map { session(1, it) },
            period = StatsPeriod.MONTH,
            today = wednesday,
        )

        assertThat(stats.readingStreak).isEqualTo(3)
        assertThat(stats.bestReadingStreak).isEqualTo(4)
    }

    @Test
    fun `a gap breaks the streak and reading today extends it`() {
        val gap = aggregateReadingStats(
            emptyList(),
            listOf(
                session(1, LocalDate.of(2026, 10, 11)),
                session(1, LocalDate.of(2026, 10, 13)),
            ),
            StatsPeriod.MONTH,
            wednesday,
        )
        val throughToday = aggregateReadingStats(
            emptyList(),
            listOf(
                session(1, LocalDate.of(2026, 10, 12)),
                session(1, LocalDate.of(2026, 10, 13)),
                session(1, LocalDate.of(2026, 10, 14)),
            ),
            StatsPeriod.MONTH,
            wednesday,
        )

        assertThat(gap.readingStreak).isEqualTo(1)
        assertThat(throughToday.readingStreak).isEqualTo(3)
    }

    @Test
    fun `current streak follows the real day when the viewed period is older`() {
        val stats = aggregateReadingStats(
            books = emptyList(),
            dailyReadings = listOf(
                session(1, LocalDate.of(2026, 9, 1)),
                session(1, LocalDate.of(2026, 10, 12)),
                session(1, LocalDate.of(2026, 10, 13)),
                session(1, LocalDate.of(2026, 10, 14)),
            ),
            period = StatsPeriod.MONTH,
            today = LocalDate.of(2026, 9, 15),
            streakAsOf = wednesday,
        )

        assertThat(stats.daysReadInPeriod).isEqualTo(1)
        assertThat(stats.readingStreak).isEqualTo(3)
    }

    @Test
    fun `average rating uses rated finished books only`() {
        val rated = aggregateReadingStats(
            books = listOf(
                finished(1, LocalDate.of(2026, 10, 1), rating = 4f),
                finished(2, LocalDate.of(2026, 10, 2), rating = 2f),
                finished(3, LocalDate.of(2026, 10, 3), rating = 0f),
                reading(4, rating = 5f),
            ),
            dailyReadings = emptyList(),
            period = StatsPeriod.MONTH,
            today = wednesday,
        )
        val unrated = aggregateReadingStats(
            books = listOf(finished(1, LocalDate.of(2026, 10, 1))),
            dailyReadings = emptyList(),
            period = StatsPeriod.MONTH,
            today = wednesday,
        )

        assertThat(rated.averageFinishedRating).isEqualTo(3f)
        assertThat(unrated.averageFinishedRating).isNull()
    }

    @Test
    fun `most read genre counts each tag on a finished book and breaks ties by name`() {
        val fantasy = Genre(id = 1, name = "Fantasy")
        val romance = Genre(id = 2, name = "Romance")
        val adventure = Genre(id = 3, name = "Aventura")
        val stats = aggregateReadingStats(
            books = listOf(
                finished(1, LocalDate.of(2026, 10, 1), genres = listOf(fantasy, romance)),
                finished(2, LocalDate.of(2026, 10, 2), genres = listOf(fantasy)),
                finished(3, LocalDate.of(2026, 9, 2), genres = listOf(adventure)),
                reading(4, genres = listOf(romance, romance)),
            ),
            dailyReadings = emptyList(),
            period = StatsPeriod.MONTH,
            today = wednesday,
        )

        assertThat(stats.mostAllReadGenre?.genre?.id).isEqualTo(fantasy.id)
        assertThat(stats.mostAllReadGenre?.genreFinished).isEqualTo(2)
        assertThat(stats.mostReadGenreInPeriod?.genre?.id).isEqualTo(fantasy.id)
        assertThat(stats.mostReadGenreInPeriod?.genreFinished).isEqualTo(2)
    }

    @Test
    fun `tied genres keep the name that comes first`() {
        val stats = aggregateReadingStats(
            books = listOf(
                finished(1, LocalDate.of(2026, 10, 1), genres = listOf(Genre(1, "Fantasy"))),
                finished(2, LocalDate.of(2026, 10, 2), genres = listOf(Genre(2, "Aventura"))),
            ),
            dailyReadings = emptyList(),
            period = StatsPeriod.MONTH,
            today = wednesday,
        )

        assertThat(stats.mostAllReadGenre?.genre?.name).isEqualTo("Aventura")
        assertThat(stats.mostAllReadGenre?.genreFinished).isEqualTo(1)
    }

    @Test
    fun `genre is empty when no finished book has one`() {
        val stats = aggregateReadingStats(
            books = listOf(finished(1, LocalDate.of(2026, 10, 1)), reading(2, genres = listOf(Genre(1, "Fantasy")))),
            dailyReadings = emptyList(),
            period = StatsPeriod.MONTH,
            today = wednesday,
        )

        assertThat(stats.mostAllReadGenre).isNull()
        assertThat(stats.mostReadGenreInPeriod).isNull()
    }

    private fun finished(
        id: Int,
        endDate: LocalDate?,
        rating: Float = 0f,
        genres: List<Genre> = emptyList(),
        physical: Boolean = false,
    ) = book(id, BookStatus.FINISHED, endDate, rating, genres, physical)

    private fun reading(
        id: Int,
        rating: Float = 0f,
        genres: List<Genre> = emptyList(),
    ) = book(id, BookStatus.READING, endDate = null, rating = rating, genres = genres)

    private fun dropped(id: Int, endDate: LocalDate) =
        book(id, BookStatus.DROPPED, endDate)

    private fun book(
        id: Int,
        status: BookStatus,
        endDate: LocalDate?,
        rating: Float = 0f,
        genres: List<Genre> = emptyList(),
        physical: Boolean = false,
    ) = Book(
        id = id,
        title = "Book $id",
        author = "Author",
        isPhysical = physical,
        status = status,
        rating = rating,
        endDate = endDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        genres = genres,
    )

    private fun session(
        bookId: Int,
        date: LocalDate,
        millis: Int = 60_000,
        speed: Int = 200,
    ) = DailyReading(
        bookId = bookId,
        date = date.toString(),
        timeSpentMillis = millis,
        reachedPercentage = 0f,
        readingSpeed = speed,
    )
}
