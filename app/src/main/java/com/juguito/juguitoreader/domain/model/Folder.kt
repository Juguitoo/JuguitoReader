package com.juguito.juguitoreader.domain.model

import com.juguito.juguitoreader.domain.enums.SyncStatus

class Folder(
    val id: Int = 0,
    val name: String,
    val colorHex: String,
    val description: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,
    val createdAt: Long = System.currentTimeMillis()
)

fun Folder.copy(
    name: String = this.name,
    colorHex: String = this.colorHex,
    description: String? = this.description,
    syncStatus: SyncStatus = this.syncStatus,
    createdAt: Long = this.createdAt
): Folder {
    return Folder(
        id = this.id,
        name = name,
        colorHex = colorHex,
        description = description,
        syncStatus = syncStatus,
        createdAt = createdAt
    )
}
