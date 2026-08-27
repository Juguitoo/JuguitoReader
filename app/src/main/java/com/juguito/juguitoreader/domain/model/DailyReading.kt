package com.juguito.juguitoreader.domain.model

class DailyReading(
    val bookId: Int,
    val date: String,
    val timeSpentMillis: Int,
    val reachedPercentage: Float,
    val readingSpeed: Int
)

fun DailyReading.copy(
    bookId: Int = this.bookId,
    date: String = this.date,
    timeSpentMillis: Int = this.timeSpentMillis,
    reachedPercentage: Float = this.reachedPercentage,
    readingSpeed: Int = this.readingSpeed
): DailyReading {
    return DailyReading(
        bookId = bookId,
        date = date,
        timeSpentMillis = timeSpentMillis,
        reachedPercentage = reachedPercentage,
        readingSpeed = readingSpeed
    )
}