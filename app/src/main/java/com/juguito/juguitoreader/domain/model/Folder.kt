package com.juguito.juguitoreader.domain.model

class Folder(
    val id: Int = 0,
    val name: String,
    val colorHex: String,
    val description: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,
    val createdAt: Long = System.currentTimeMillis()
)