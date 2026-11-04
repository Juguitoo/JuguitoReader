package com.juguito.juguitoreader.domain.model

data class DeletedBookSnapshot(
    val book: Book,
    val dailyReadings: List<DailyReading>,
    val readingProgress: ReadingProgress?,
    val bookmarks: List<Bookmark> = emptyList()
)