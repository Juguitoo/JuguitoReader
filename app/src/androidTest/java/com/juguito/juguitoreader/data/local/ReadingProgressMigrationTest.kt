package com.juguito.juguitoreader.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase.Companion.MIGRATION_10_11
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReadingProgressMigrationTest {

    private lateinit var db: SupportSQLiteDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("reading-progress-migration-test")
                .callback(object : SupportSQLiteOpenHelper.Callback(10) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS books (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                title TEXT NOT NULL,
                                author TEXT NOT NULL,
                                publisher TEXT,
                                series TEXT,
                                series_order REAL,
                                is_physical INTEGER NOT NULL,
                                status TEXT NOT NULL,
                                rating REAL NOT NULL,
                                comment TEXT,
                                start_date INTEGER,
                                end_date INTEGER,
                                cover_url TEXT,
                                local_file_path TEXT,
                                sync_status TEXT NOT NULL,
                                created_at INTEGER NOT NULL
                            )
                            """.trimIndent()
                        )
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS reading_progress (
                                book_id INTEGER NOT NULL,
                                total_chapters INTEGER NOT NULL DEFAULT 0,
                                last_chapter_index INTEGER NOT NULL,
                                scroll_position REAL NOT NULL,
                                last_read_at INTEGER NOT NULL,
                                sync_status TEXT NOT NULL,
                                PRIMARY KEY(book_id)
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
    fun migration_10_11_preserves_valid_progress_and_drops_orphans() {
        db.execSQL(
            """
            INSERT INTO books (id, title, author, is_physical, status, rating, sync_status, created_at)
            VALUES (1, 'Title', 'Author', 0, 'READING', 0, 'SYNCED', 0)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO reading_progress (
                book_id, total_chapters, last_chapter_index, scroll_position, last_read_at, sync_status
            ) VALUES (1, 20, 5, 0.75, 100, 'SYNCED')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO reading_progress (
                book_id, total_chapters, last_chapter_index, scroll_position, last_read_at, sync_status
            ) VALUES (999, 10, 1, 0.0, 50, 'SYNCED')
            """.trimIndent()
        )

        MIGRATION_10_11.migrate(db)

        val cursor = db.query(
            "SELECT book_id, last_chapter_index, scroll_position FROM reading_progress"
        )
        assertThat(cursor.count).isEqualTo(1)
        cursor.moveToFirst()
        assertThat(cursor.getInt(0)).isEqualTo(1)
        assertThat(cursor.getInt(1)).isEqualTo(5)
        assertThat(cursor.getFloat(2)).isWithin(0.001f).of(0.75f)
        cursor.close()
    }
}
