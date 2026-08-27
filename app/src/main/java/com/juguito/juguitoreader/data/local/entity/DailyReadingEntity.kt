package com.juguito.juguitoreader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "daily_reading",
    primaryKeys = ["book_id", "date"],
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
data class DailyReadingEntity (
    @ColumnInfo(name = "book_id") val bookId: Int,
    val date: String,
    @ColumnInfo(name = "time_spent_millis") val timeSpentMillis: Int,
    @ColumnInfo(name = "reached_percentage") val reachedPercentage: Float,
    @ColumnInfo(name = "reading_speed") val readingSpeed: Int
)