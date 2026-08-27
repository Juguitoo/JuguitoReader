package com.juguito.juguitoreader.data.mapper

import com.juguito.juguitoreader.data.local.entity.DailyReadingEntity
import com.juguito.juguitoreader.domain.model.DailyReading

fun DailyReadingEntity.toDomain(): DailyReading {
    return DailyReading(
        bookId = bookId,
        date = date,
        timeSpentMillis = timeSpentMillis,
        reachedPercentage = reachedPercentage,
        readingSpeed = readingSpeed
    )
}

fun DailyReading.toEntity(): DailyReadingEntity {
    return DailyReadingEntity(
        bookId = bookId,
        date = date,
        timeSpentMillis = timeSpentMillis,
        reachedPercentage = reachedPercentage,
        readingSpeed = readingSpeed
    )
}