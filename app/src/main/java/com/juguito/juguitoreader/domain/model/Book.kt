package com.juguito.juguitoreader.domain.model

import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.enums.SyncStatus

class Book(
    val id: Int = 0,
    val title: String,
    val author: String,
    val publisher: String? = null,
    val series: String? = null,
    val seriesOrder: Double? = null,
    val isPhysical: Boolean,
    val status: BookStatus = BookStatus.PENDING,
    val rating: Float = 0.0f,
    val comment: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val coverUrl: String? = null,
    val localFilePath: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,

    val folders: List<Folder> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val readingProgress: ReadingProgress? = null,
    val createdAt: Long = System.currentTimeMillis()
)

fun Book.copy(
    title: String = this.title,
    author: String = this.author,
    publisher: String? = this.publisher,
    series: String? = this.series,
    seriesOrder: Double? = this.seriesOrder,
    isPhysical: Boolean = this.isPhysical,
    status: BookStatus = this.status,
    rating: Float = this.rating,
    comment: String? = this.comment,
    startDate: Long? = this.startDate,
    endDate: Long? = this.endDate,
    coverUrl: String? = this.coverUrl,
    localFilePath: String? = this.localFilePath,
    syncStatus: SyncStatus = this.syncStatus,
    folders: List<Folder> = this.folders,
    genres: List<Genre> = this.genres,
    readingProgress: ReadingProgress? = this.readingProgress,
    createdAt: Long = this.createdAt
): Book {
    return Book(
        id = this.id,
        title = title,
        author = author,
        publisher = publisher,
        series = series,
        seriesOrder = seriesOrder,
        isPhysical = isPhysical,
        status = status,
        rating = rating,
        comment = comment,
        startDate = startDate,
        endDate = endDate,
        coverUrl = coverUrl,
        localFilePath = localFilePath,
        syncStatus = syncStatus,
        folders = folders,
        genres = genres,
        readingProgress = readingProgress,
        createdAt = createdAt
    )
}