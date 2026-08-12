package com.juguito.juguitoreader.domain.model

import com.juguito.juguitoreader.domain.enums.SyncStatus

class Genre(
    val id: Int = 0,
    val name: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,
    val createdAt: Long = System.currentTimeMillis()
)

fun Genre.copy(
    name: String = this.name,
    syncStatus: SyncStatus = this.syncStatus,
    createdAt: Long = this.createdAt
): Genre {
    return Genre(
        id = this.id,
        name = name,
        syncStatus = syncStatus,
        createdAt = createdAt
    )
}
