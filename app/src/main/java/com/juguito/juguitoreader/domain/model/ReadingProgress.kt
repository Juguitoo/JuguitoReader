package com.juguito.juguitoreader.domain.model

class ReadingProgress (
    val bookId: Int,
    val totalChapters: Int,
    val lastChapterIndex: Int,
    val scrollPosition: Float,
    val lastReadAt: Long
) {
    val percentage: Int
        get() = if (totalChapters > 0) {
            ((lastChapterIndex.toFloat() / totalChapters) * 100).toInt()
        } else {
            0
        }
}

fun ReadingProgress.copy(
    bookId: Int = this.bookId,
    totalChapters: Int = this.totalChapters,
    lastChapterIndex: Int = this.lastChapterIndex,
    scrollPosition: Float = this.scrollPosition,
    lastReadAt: Long = this.lastReadAt
): ReadingProgress {
    return ReadingProgress(
        bookId = bookId,
        totalChapters = totalChapters,
        lastChapterIndex = lastChapterIndex,
        scrollPosition = scrollPosition,
        lastReadAt = lastReadAt
    )
}