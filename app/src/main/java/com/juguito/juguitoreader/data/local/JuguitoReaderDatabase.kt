package com.juguito.juguitoreader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.juguito.juguitoreader.data.local.dao.BookDAO
import com.juguito.juguitoreader.data.local.dao.DailyReadingDAO
import com.juguito.juguitoreader.data.local.dao.FolderDAO
import com.juguito.juguitoreader.data.local.dao.GenreDAO
import com.juguito.juguitoreader.data.local.dao.ReadingProgressDAO
import com.juguito.juguitoreader.data.local.entity.BookEntity
import com.juguito.juguitoreader.data.local.entity.FolderEntity
import com.juguito.juguitoreader.data.local.entity.GenreEntity
import com.juguito.juguitoreader.data.local.entity.BookFolderCrossRef
import com.juguito.juguitoreader.data.local.entity.BookGenreCrossRef
import com.juguito.juguitoreader.data.local.entity.DailyReadingEntity
import com.juguito.juguitoreader.data.local.entity.ReadingProgressEntity

@Database(
    entities = [
        BookEntity::class,
        FolderEntity::class,
        GenreEntity::class,
        ReadingProgressEntity::class,
        DailyReadingEntity::class,
        BookFolderCrossRef::class,
        BookGenreCrossRef::class
    ],
    version = 10,
    exportSchema = true
)

@TypeConverters(RoomConverters::class)
abstract class JuguitoReaderDatabase : RoomDatabase() {
    abstract val bookDAO: BookDAO
    abstract val folderDAO: FolderDAO
    abstract val genreDAO: GenreDAO
    abstract val readingProgressDAO: ReadingProgressDAO
    abstract val dailyReadingDAO: DailyReadingDAO

    companion object {
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE reading_progress_new (
                        book_id INTEGER NOT NULL PRIMARY KEY,
                        last_chapter_index INTEGER NOT NULL,
                        scroll_position REAL NOT NULL,
                        last_read_at INTEGER NOT NULL,
                        sync_status TEXT NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO reading_progress_new (book_id, last_chapter_index, scroll_position, last_read_at, sync_status)
                    SELECT book_id, last_chapter_index, 0.0, last_read_at, sync_status FROM reading_progress
                """.trimIndent())

                db.execSQL("DROP TABLE reading_progress")
                db.execSQL("ALTER TABLE reading_progress_new RENAME TO reading_progress")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reading_progress ADD COLUMN total_chapters INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `daily_reading` (
                        `book_id` INTEGER NOT NULL, 
                        `date` TEXT NOT NULL, 
                        `time_spent_millis` INTEGER NOT NULL, 
                        `reached_percentage` REAL NOT NULL, 
                        PRIMARY KEY(`book_id`, `date`), 
                        FOREIGN KEY(`book_id`) REFERENCES `books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_reading_book_id` ON `daily_reading` (`book_id`)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    ALTER TABLE daily_reading ADD COLUMN reading_speed INTEGER NOT NULL DEFAULT 0
                """.trimIndent())
            }
        }
    }
}
