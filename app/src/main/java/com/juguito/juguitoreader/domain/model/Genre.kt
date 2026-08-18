package com.juguito.juguitoreader.domain.model

import com.juguito.juguitoreader.domain.enums.SyncStatus

class Genre(
    val id: Int = 0,
    val name: String,
    val bookCount: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,
    val createdAt: Long = System.currentTimeMillis()
)

fun Genre.copy(
    name: String = this.name,
    bookCount: Int = this.bookCount,
    syncStatus: SyncStatus = this.syncStatus,
    createdAt: Long = this.createdAt
): Genre {
    return Genre(
        id = this.id,
        name = name,
        bookCount = bookCount,
        syncStatus = syncStatus,
        createdAt = createdAt
    )
}
