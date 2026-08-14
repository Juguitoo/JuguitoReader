package com.juguito.juguitoreader.domain.model

class ReadingProgress (
    val bookId: Int,
    val lastChapterIndex: Int,
    val scrollPosition: Float,
    val lastReadAt: Long
)

fun ReadingProgress.copy(
    bookId: Int = this.bookId,
    lastChapterIndex: Int = this.lastChapterIndex,
    scrollPosition: Float = this.scrollPosition,
    lastReadAt: Long = this.lastReadAt
): ReadingProgress {
    return ReadingProgress(
        bookId = bookId,
        lastChapterIndex = lastChapterIndex,
        scrollPosition = scrollPosition,
        lastReadAt = lastReadAt
    )
}