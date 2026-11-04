package com.juguito.juguitoreader.domain.enums

enum class BookStatus {
    PENDING,
    READING,
    FINISHED,
    DROPPED
}

enum class Language(val code: String) {
    SYSTEM(""),
    SPANISH("es"),
    ENGLISH("en")
}


enum class StatsPeriod {
    WEEK,
    MONTH,
    YEAR
}

enum class BookmarkKind {
    BOOKMARK,
    NOTE,
    HIGHLIGHT
}

enum class BookmarkColor {
    AMBER,
    GREEN,
    BLUE,
    PINK,
    LILAC
}