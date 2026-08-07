package com.juguito.juguitoreader.data.mapper

import com.juguito.juguitoreader.data.local.entity.ReadingProgressEntity
import com.juguito.juguitoreader.domain.model.ReadingProgress

fun ReadingProgressEntity.toDomain(): ReadingProgress {
    return ReadingProgress(
        bookId = bookId,
        lastChapterIndex = lastChapterIndex,
        scrollPosition = scrollPosition,
        lastReadAt = lastReadAt
    )
}

fun ReadingProgress.toEntity(): ReadingProgressEntity {
    return ReadingProgressEntity(
        bookId = bookId,
        lastChapterIndex = lastChapterIndex,
        scrollPosition = scrollPosition,
        lastReadAt = lastReadAt
    )
}