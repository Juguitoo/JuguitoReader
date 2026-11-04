package com.juguito.juguitoreader.data.local

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase.Companion.MIGRATION_13_14
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookmarkMigrationTest {

    private lateinit var db: SupportSQLiteDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("bookmark-migration-13-14")
                .callback(object : SupportSQLiteOpenHelper.Callback(13) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `books` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `title` TEXT NOT NULL,
                                `author` TEXT NOT NULL,
                                `is_physical` INTEGER NOT NULL,
                                `is_favourite` INTEGER NOT NULL,
                                `status` TEXT NOT NULL,
                                `rating` REAL NOT NULL,
                                `created_at` INTEGER NOT NULL
                            )
                            """.trimIndent()
                        )
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `reading_progress` (
                                `book_id` INTEGER NOT NULL,
                                `total_chapters` INTEGER NOT NULL DEFAULT 0,
                                `last_chapter_index` INTEGER NOT NULL,
                                `scroll_position` REAL NOT NULL,
                                `last_read_at` INTEGER NOT NULL,
                                PRIMARY KEY(`book_id`),
                                FOREIGN KEY(`book_id`) REFERENCES `books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                            )
                            """.trimIndent()
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
    fun migration_13_14_creates_empty_bookmark_table_and_keeps_existing_rows() {
        db.execSQL(
            """
            INSERT INTO books (id, title, author, is_physical, is_favourite, status, rating, created_at)
            VALUES (1, 'Title', 'Author', 0, 0, 'READING', 0, 123)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO reading_progress (
                book_id, total_chapters, last_chapter_index, scroll_position, last_read_at
            ) VALUES (1, 20, 5, 0.75, 100)
            """.trimIndent()
        )

        MIGRATION_13_14.migrate(db)

        assertThat(queryString("SELECT title FROM books WHERE id = 1")).isEqualTo("Title")
        assertThat(queryLong("SELECT last_chapter_index FROM reading_progress WHERE book_id = 1")).isEqualTo(5)
        assertThat(queryLong("SELECT COUNT(*) FROM bookmark")).isEqualTo(0)
        assertThat(columnNames("bookmark")).containsAtLeast("image_src", "chapter_href", "quote", "note_text")
        assertThat(
            queryString(
                "SELECT name FROM sqlite_master WHERE type = 'index' AND name = 'index_bookmark_book_id'"
            )
        ).isEqualTo("index_bookmark_book_id")
    }

    private fun queryString(sql: String): String? {
        db.query(sql).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    private fun queryLong(sql: String): Long {
        db.query(sql).use { cursor ->
            cursor.moveToFirst()
            return cursor.getLong(0)
        }
    }

    private fun columnNames(table: String): List<String> {
        val names = mutableListOf<String>()
        db.query("PRAGMA table_info(`$table`)").use { cursor: Cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                names += cursor.getString(nameIndex)
            }
        }
        return names
    }
}
