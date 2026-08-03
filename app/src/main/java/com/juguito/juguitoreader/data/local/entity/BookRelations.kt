package com.juguito.juguitoreader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation

@Entity(tableName = "book_folders",
    primaryKeys = ["book_id", "folder_id"])
data class BookFolderCrossRef(
    @ColumnInfo(name = "book_id") val bookId: Int,
    @ColumnInfo(name = "folder_id") val folderId: Int
)

@Entity(tableName = "book_genres",
    primaryKeys = ["book_id", "genre_id"])
data class BookGenreCrossRef(
    @ColumnInfo(name = "book_id") val bookId: Int,
    @ColumnInfo(name = "genre_id") val genreId: Int
)

data class BookWithDetails(
    @Embedded val book: BookEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookFolderCrossRef::class,
            parentColumn = "book_id",
            entityColumn = "folder_id"
        )
    )
    val folders: List<FolderEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookGenreCrossRef::class,
            parentColumn = "book_id",
            entityColumn = "genre_id"
        )
    )
    val genres: List<GenreEntity>
)
