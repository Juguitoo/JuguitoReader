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
