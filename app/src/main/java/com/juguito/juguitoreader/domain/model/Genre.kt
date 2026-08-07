package com.juguito.juguitoreader.domain.model

import com.juguito.juguitoreader.domain.enums.SyncStatus

class Genre(
    val id: Int = 0,
    val name: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,
    val createdAt: Long = System.currentTimeMillis()
)