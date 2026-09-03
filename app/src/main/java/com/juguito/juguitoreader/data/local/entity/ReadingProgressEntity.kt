package com.juguito.juguitoreader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_progress",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["book_id"])
    ]
)
data class ReadingProgressEntity (
    @ColumnInfo(name = "book_id") @PrimaryKey val bookId: Int,
    @ColumnInfo(name = "total_chapters", defaultValue = "0") val totalChapters: Int = 0,
    @ColumnInfo(name = "last_chapter_index") val lastChapterIndex: Int,
    @ColumnInfo(name = "scroll_position") val scrollPosition: Float,
    @ColumnInfo(name = "last_read_at") val lastReadAt: Long
)