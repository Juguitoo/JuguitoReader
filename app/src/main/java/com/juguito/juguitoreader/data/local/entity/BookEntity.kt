package com.juguito.juguitoreader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.enums.SyncStatus

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val publisher: String? = null,
    val series: String? = null,
    @ColumnInfo(name = "series_order") val seriesOrder: Double? = null,
    @ColumnInfo(name = "is_physical") val isPhysical: Boolean,
    val status: BookStatus = BookStatus.PENDING,
    val rating: Float = 0.0f,
    val comment: String? = null,
    @ColumnInfo(name = "start_date") val startDate: Long? = null,
    @ColumnInfo(name = "end_date") val endDate: Long? = null,
    @ColumnInfo(name = "cover_url") val coverUrl: String? = null,
    @ColumnInfo(name = "local_file_path") val localFilePath: String? = null,
    @ColumnInfo(name = "sync_status") val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,
    @ColumnInfo(name = "created_at") val createdAt: Long
    )
