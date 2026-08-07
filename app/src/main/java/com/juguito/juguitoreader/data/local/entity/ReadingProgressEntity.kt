package com.juguito.juguitoreader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.juguito.juguitoreader.domain.enums.SyncStatus

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity (
    @ColumnInfo(name = "book_id") @PrimaryKey val bookId: Int,
    @ColumnInfo(name = "last_chapter_index") val lastChapterIndex: Int,
    @ColumnInfo(name = "scroll_position") val scrollPosition: Int,
    @ColumnInfo(name = "last_read_at") val lastReadAt: Long,
    @ColumnInfo(name = "sync_status") val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE
    )