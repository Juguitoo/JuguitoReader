package com.juguito.juguitoreader.domain.model

import java.time.LocalDate

class ReadingStats(
    val totalFinished: Int,
    val finishedInPeriod: List<Book>,
    val readingStreak: Int,
    val bestReadingStreak: Int,
    val readingMinutes: List<ReadingStatsBucket>,
    val readingTimeComparison: ReadingTimeComparison,
    val daysReadInPeriod: Int,
    val readingAverageVelocity: Int,
    val averageFinishedRating: Float?,
    val mostAllReadGenre: ReadingGenreStat?,
    val mostReadGenreInPeriod: ReadingGenreStat?,
)

class ReadingStatsBucket(
    val start: LocalDate,
    val minutes: Int,
)

class ReadingGenreStat(
    val genre: Genre,
    val genreFinished: Int,
)

class ReadingTimeComparison(
    val currentMinutes: Int,
    val previousMinutes: Int?,
)
