package com.juguito.juguitoreader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "genres",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class GenreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

data class GenreWithCountEntity(
    @Embedded val genre: GenreEntity,
    val bookCount: Int
)
