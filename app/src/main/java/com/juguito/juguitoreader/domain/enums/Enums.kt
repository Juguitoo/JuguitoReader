package com.juguito.juguitoreader.domain.enums

enum class SyncStatus(val displayName: String) {
    SYNCED("Sincronizado"),
    PENDING_CREATE("Pendiente de crear"),
    PENDING_UPDATE("Pendiente de actualizar"),
    PENDING_DELETE("Pendiente de borrar")
}

enum class BookStatus(val displayName: String) {
    PENDING("Pendiente"),
    READING("Leyendo"),
    FINISHED("Finalizado"),
    DROPPED("Abandonado")
}