package com.juguito.juguitoreader.domain.model

class ReadingProgress (
    val bookId: Int,
    val lastChapterIndex: Int,
    val scrollPosition: Int,
    val lastReadAt: Long
)