package com.juguito.juguitoreader.domain.usecase.bookmark

import com.juguito.juguitoreader.domain.enums.BookmarkColor

sealed interface NewBookmark {
    val bookId: Int
    val chapterHref: String
    val quote: String
    val prefix: String
    val suffix: String
    val scrollPosition: Float

    data class Position(
        override val bookId: Int,
        override val chapterHref: String,
        override val quote: String,
        override val prefix: String,
        override val suffix: String,
        override val scrollPosition: Float,
        val imageSrc: String? = null
    ) : NewBookmark

    data class Highlight(
        override val bookId: Int,
        override val chapterHref: String,
        override val quote: String,
        override val prefix: String,
        override val suffix: String,
        override val scrollPosition: Float,
        val color: BookmarkColor
    ) : NewBookmark

    data class Note(
        override val bookId: Int,
        override val chapterHref: String,
        override val quote: String,
        override val prefix: String,
        override val suffix: String,
        override val scrollPosition: Float,
        val color: BookmarkColor,
        val noteText: String
    ) : NewBookmark
}