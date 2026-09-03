package com.juguito.juguitoreader.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase.Companion.MIGRATION_11_12
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncStatusRemovalMigrationTest {

    private lateinit var db: SupportSQLiteDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("sync-status-removal-migration-test")
                .callback(object : SupportSQLiteOpenHelper.Callback(11) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `books` (
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
                                `sync_status` TEXT NOT NULL,
                                `created_at` INTEGER NOT NULL
                            )
                            """.trimIndent()
                        )
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `folders` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `name` TEXT NOT NULL,
                                `description` TEXT,
                                `color_hex` TEXT NOT NULL,
                                `sync_status` TEXT NOT NULL,
                                `created_at` INTEGER NOT NULL
                            )
                            """.trimIndent()
                        )
                        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_folders_name` ON `folders` (`name`)")
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `genres` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `name` TEXT NOT NULL,
                                `sync_status` TEXT NOT NULL,
                                `created_at` INTEGER NOT NULL
                            )
                            """.trimIndent()
                        )
                        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_genres_name` ON `genres` (`name`)")
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `reading_progress` (
                                `book_id` INTEGER NOT NULL,
                                `total_chapters` INTEGER NOT NULL DEFAULT 0,
                                `last_chapter_index` INTEGER NOT NULL,
                                `scroll_position` REAL NOT NULL,
                                `last_read_at` INTEGER NOT NULL,
                                `sync_status` TEXT NOT NULL,
                                PRIMARY KEY(`book_id`),
                                FOREIGN KEY(`book_id`) REFERENCES `books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                            )
                            """.trimIndent()
                        )
                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_reading_progress_book_id` ON `reading_progress` (`book_id`)"
                        )
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) = Unit
                })
                .build()
        )
        db = helper.writableDatabase
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun migration_11_12_drops_sync_status_and_preserves_rows() {
        db.execSQL(
            """
            INSERT INTO books (id, title, author, is_physical, status, rating, sync_status, created_at)
            VALUES (1, 'Title', 'Author', 0, 'READING', 0, 'PENDING_CREATE', 123)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO folders (id, name, description, color_hex, sync_status, created_at)
            VALUES (2, 'Folder', 'Desc', '#FFF', 'SYNCED', 456)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO genres (id, name, sync_status, created_at)
            VALUES (3, 'Genre', 'PENDING_UPDATE', 789)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO reading_progress (
                book_id, total_chapters, last_chapter_index, scroll_position, last_read_at, sync_status
            ) VALUES (1, 20, 5, 0.75, 100, 'SYNCED')
            """.trimIndent()
        )

        MIGRATION_11_12.migrate(db)

        listOf("books", "folders", "genres", "reading_progress").forEach { table ->
            assertThat(tableHasColumn(table, "sync_status")).isFalse()
        }

        db.query("SELECT id, title, created_at FROM books").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            cursor.moveToFirst()
            assertThat(cursor.getInt(0)).isEqualTo(1)
            assertThat(cursor.getString(1)).isEqualTo("Title")
            assertThat(cursor.getLong(2)).isEqualTo(123)
        }
        db.query("SELECT id, name, color_hex FROM folders").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            cursor.moveToFirst()
            assertThat(cursor.getInt(0)).isEqualTo(2)
            assertThat(cursor.getString(1)).isEqualTo("Folder")
            assertThat(cursor.getString(2)).isEqualTo("#FFF")
        }
        db.query("SELECT id, name FROM genres").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            cursor.moveToFirst()
            assertThat(cursor.getInt(0)).isEqualTo(3)
            assertThat(cursor.getString(1)).isEqualTo("Genre")
        }
        db.query(
            "SELECT book_id, last_chapter_index, scroll_position FROM reading_progress"
        ).use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            cursor.moveToFirst()
            assertThat(cursor.getInt(0)).isEqualTo(1)
            assertThat(cursor.getInt(1)).isEqualTo(5)
            assertThat(cursor.getFloat(2)).isWithin(0.001f).of(0.75f)
        }

        db.query("PRAGMA index_list(`folders`)").use { cursor ->
            assertThat(indexExists(cursor, "index_folders_name")).isTrue()
        }
        db.query("PRAGMA index_list(`genres`)").use { cursor ->
            assertThat(indexExists(cursor, "index_genres_name")).isTrue()
        }
    }

    private fun tableHasColumn(table: String, column: String): Boolean {
        db.query("PRAGMA table_info(`$table`)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == column) return true
            }
        }
        return false
    }

    private fun indexExists(cursor: android.database.Cursor, indexName: String): Boolean {
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (cursor.getString(nameIndex) == indexName) return true
        }
        return false
    }
}
