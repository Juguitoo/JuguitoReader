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