package com.juguito.juguitoreader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.juguito.juguitoreader.domain.enums.BookmarkColor
import com.juguito.juguitoreader.domain.enums.BookmarkKind

@Entity(
    tableName = "bookmark",
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
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "book_id") val bookId: Int,
    val kind: BookmarkKind,
    @ColumnInfo(name = "chapter_href") val chapterHref: String,
    val quote: String,
    val prefix: String,
    val suffix: String,
    @ColumnInfo(name = "image_src") val imageSrc: String?,
    val color: BookmarkColor?,
    @ColumnInfo(name = "note_text") val noteText: String?,
    @ColumnInfo(name = "scroll_position") val scrollPosition: Float,
    @ColumnInfo(name = "created_at") val createdAt: Long
)