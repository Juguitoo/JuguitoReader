package com.juguito.juguitoreader.domain.model

enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE
}

enum class BookStatus {
    PENDING,
    READING,
    FINISHED,
    DROPPED
}