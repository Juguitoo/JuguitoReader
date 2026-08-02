package com.juguito.juguitoreader.domain.model

class Book(
    val id: Int = 0,
    val title: String,
    val author: String,
    val isPhysical: Boolean,
    val status: BookStatus = BookStatus.PENDING,
    val rating: Float = 0.0f,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val coverUrl: String? = null,
    val localFilePath: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,

    val folders: List<Folder> = emptyList(),
    val genres: List<Genre> = emptyList()
)