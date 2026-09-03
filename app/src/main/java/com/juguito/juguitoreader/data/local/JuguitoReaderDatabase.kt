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
    version = 12,
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

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    DELETE FROM reading_progress
                    WHERE book_id NOT IN (SELECT id FROM books)
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reading_progress_new` (
                        `book_id` INTEGER NOT NULL,
                        `total_chapters` INTEGER NOT NULL DEFAULT 0,
                        `last_chapter_index` INTEGER NOT NULL,
                        `scroll_position` REAL NOT NULL,
                        `last_read_at` INTEGER NOT NULL,
                        `sync_status` TEXT NOT NULL,
                        PRIMARY KEY(`book_id`),
                        FOREIGN KEY(`book_id`) REFERENCES `books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO reading_progress_new (
                        book_id, total_chapters, last_chapter_index, scroll_position, last_read_at, sync_status
                    )
                    SELECT book_id, total_chapters, last_chapter_index, scroll_position, last_read_at, sync_status
                    FROM reading_progress
                """.trimIndent())

                db.execSQL("DROP TABLE reading_progress")
                db.execSQL("ALTER TABLE reading_progress_new RENAME TO reading_progress")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_reading_progress_book_id` ON `reading_progress` (`book_id`)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `books_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `author` TEXT NOT NULL,
                        `publisher` TEXT,
                        `series` TEXT,
                        `series_order` REAL,
                        `is_physical` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `rating` REAL NOT NULL,
                        `comment` TEXT,
                        `start_date` INTEGER,
                        `end_date` INTEGER,
                        `cover_url` TEXT,
                        `local_file_path` TEXT,
                        `created_at` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO `books_new` (
                        id, title, author, publisher, series, series_order, is_physical, status, rating,
                        comment, start_date, end_date, cover_url, local_file_path, created_at
                    )
                    SELECT
                        id, title, author, publisher, series, series_order, is_physical, status, rating,
                        comment, start_date, end_date, cover_url, local_file_path, created_at
                    FROM `books`
                """.trimIndent())
                db.execSQL("DROP TABLE `books`")
                db.execSQL("ALTER TABLE `books_new` RENAME TO `books`")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `folders_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT,
                        `color_hex` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO `folders_new` (id, name, description, color_hex, created_at)
                    SELECT id, name, description, color_hex, created_at FROM `folders`
                """.trimIndent())
                db.execSQL("DROP TABLE `folders`")
                db.execSQL("ALTER TABLE `folders_new` RENAME TO `folders`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_folders_name` ON `folders` (`name`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `genres_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO `genres_new` (id, name, created_at)
                    SELECT id, name, created_at FROM `genres`
                """.trimIndent())
                db.execSQL("DROP TABLE `genres`")
                db.execSQL("ALTER TABLE `genres_new` RENAME TO `genres`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_genres_name` ON `genres` (`name`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reading_progress_new` (
                        `book_id` INTEGER NOT NULL,
                        `total_chapters` INTEGER NOT NULL DEFAULT 0,
                        `last_chapter_index` INTEGER NOT NULL,
                        `scroll_position` REAL NOT NULL,
                        `last_read_at` INTEGER NOT NULL,
                        PRIMARY KEY(`book_id`),
                        FOREIGN KEY(`book_id`) REFERENCES `books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO `reading_progress_new` (
                        book_id, total_chapters, last_chapter_index, scroll_position, last_read_at
                    )
                    SELECT book_id, total_chapters, last_chapter_index, scroll_position, last_read_at
                    FROM `reading_progress`
                """.trimIndent())
                db.execSQL("DROP TABLE `reading_progress`")
                db.execSQL("ALTER TABLE `reading_progress_new` RENAME TO `reading_progress`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_reading_progress_book_id` ON `reading_progress` (`book_id`)")
            }
        }
    }
}
