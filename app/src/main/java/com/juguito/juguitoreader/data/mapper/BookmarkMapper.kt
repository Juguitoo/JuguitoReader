package com.juguito.juguitoreader.data.mapper

import com.juguito.juguitoreader.data.local.entity.BookmarkEntity
import com.juguito.juguitoreader.domain.model.Bookmark

fun BookmarkEntity.toDomain(): Bookmark {
    return Bookmark(
        id = id,
        bookId = bookId,
        kind = kind,
        chapterHref = chapterHref,
        color = color,
        quote = quote,
        prefix = prefix,
        suffix = suffix,
        imageSrc = imageSrc,
        noteText = noteText,
        scrollPosition = scrollPosition,
        createdAt = createdAt
    )
}

fun Bookmark.toEntity(): BookmarkEntity {
    return BookmarkEntity(
        id = id,
        bookId = bookId,
        kind = kind,
        chapterHref = chapterHref,
        color = color,
        quote = quote,
        prefix = prefix,
        suffix = suffix,
        imageSrc = imageSrc,
        noteText = noteText,
        scrollPosition = scrollPosition,
        createdAt = createdAt
    )
}