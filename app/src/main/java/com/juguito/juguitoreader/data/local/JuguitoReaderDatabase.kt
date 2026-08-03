package com.juguito.juguitoreader.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.juguito.juguitoreader.data.local.dao.BookDAO
import com.juguito.juguitoreader.data.local.dao.FolderDAO
import com.juguito.juguitoreader.data.local.dao.GenreDAO
import com.juguito.juguitoreader.data.local.entity.BookEntity
import com.juguito.juguitoreader.data.local.entity.FolderEntity
import com.juguito.juguitoreader.data.local.entity.GenreEntity
import com.juguito.juguitoreader.data.local.entity.BookFolderCrossRef
import com.juguito.juguitoreader.data.local.entity.BookGenreCrossRef

@Database(
    entities = [
        BookEntity::class,
        FolderEntity::class,
        GenreEntity::class,
        BookFolderCrossRef::class,
        BookGenreCrossRef::class
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)

@TypeConverters(RoomConverters::class)
abstract class JuguitoReaderDatabase : RoomDatabase() {
    abstract val bookDAO: BookDAO
    abstract val folderDAO: FolderDAO
    abstract val genreDAO: GenreDAO
}