package com.juguito.juguitoreader.domain.enums

enum class BookStatus(val displayName: String) {
    PENDING("Pendiente"),
    READING("Leyendo"),
    FINISHED("Finalizado"),
    DROPPED("Abandonado")
}

enum class Language(val displayName: String, val code: String) {
    SYSTEM("Sistema", ""),
    SPANISH("Español", "es"),
    ENGLISH("Inglés", "en")
}