package com.juguito.juguitoreader.domain.model

class Genre(
    val id: Int = 0,
    val name: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE
)