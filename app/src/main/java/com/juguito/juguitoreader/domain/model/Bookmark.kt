package com.juguito.juguitoreader.domain.model

import com.juguito.juguitoreader.domain.enums.BookmarkColor
import com.juguito.juguitoreader.domain.enums.BookmarkKind

class Bookmark(
    val id: Int = 0,
    val bookId: Int,
    val kind: BookmarkKind,
    val chapterHref: String,
    val quote: String,
    val prefix: String,
    val suffix: String,
    val imageSrc: String?,
    val color: BookmarkColor?,
    val noteText: String?,
    val scrollPosition: Float,
    val createdAt: Long
)

fun Bookmark.copy(
    kind: BookmarkKind = this.kind,
    color: BookmarkColor? = this.color,
    noteText: String? = this.noteText
): Bookmark {
    return Bookmark(
        id = id,
        bookId = bookId,
        kind = kind,
        chapterHref = chapterHref,
        quote = quote,
        prefix = prefix,
        suffix = suffix,
        imageSrc = imageSrc,
        color = color,
        noteText = noteText,
        scrollPosition = scrollPosition,
        createdAt = createdAt
    )
}