package com.juguito.juguitoreader.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase
import com.juguito.juguitoreader.data.local.entity.ReadingProgressEntity
import com.juguito.juguitoreader.domain.enums.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReadingProgressDAOTest {

    private lateinit var database: JuguitoReaderDatabase
    private lateinit var readingProgressDAO: ReadingProgressDAO

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            JuguitoReaderDatabase::class.java
        ).allowMainThreadQueries().build()
        readingProgressDAO = database.readingProgressDAO
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertReadingProgresses_and_getAll() = runBlocking {
        val list = listOf(
            ReadingProgressEntity(bookId = 1, lastChapterIndex = 0, scrollPosition = 0f, lastReadAt = 0L),
            ReadingProgressEntity(bookId = 2, lastChapterIndex = 0, scrollPosition = 0f, lastReadAt = 1L)
        )
        readingProgressDAO.insertReadingProgresses(list)
        
        val result = readingProgressDAO.getAllReadingProgress().first()
        assertThat(result).hasSize(2)
    }

    @Test
    fun getReadingProgressById_returns_correct_one() = runBlocking {
        val p = ReadingProgressEntity(bookId = 5, lastChapterIndex = 10, scrollPosition = 0.5f, lastReadAt = 100L)
        readingProgressDAO.insertReadingProgress(p)
        
        val result = readingProgressDAO.getReadingProgressById(5)
        assertThat(result?.lastChapterIndex).isEqualTo(10)
    }

    @Test
    fun getUnsyncedReadingProgresses_returns_only_unsynced() = runBlocking {
        val p1 = ReadingProgressEntity(bookId = 1, lastChapterIndex = 0, scrollPosition = 0f, lastReadAt = 0L, syncStatus = SyncStatus.SYNCED)
        val p2 = ReadingProgressEntity(bookId = 2, lastChapterIndex = 0, scrollPosition = 0f, lastReadAt = 1L, syncStatus = SyncStatus.PENDING_CREATE)
        
        readingProgressDAO.insertReadingProgresses(listOf(p1, p2))
        
        val result = readingProgressDAO.getUnsyncedReadingProgresses()
        assertThat(result).hasSize(1)
        assertThat(result[0].bookId).isEqualTo(2)
    }

    @Test
    fun deleteReadingProgressById_removes_it() = runBlocking {
        readingProgressDAO.insertReadingProgress(ReadingProgressEntity(bookId = 1, lastChapterIndex = 0, scrollPosition = 0f, lastReadAt = 0L))
        readingProgressDAO.deleteReadingProgressById(1)
        assertThat(readingProgressDAO.getReadingProgressById(1)).isNull()
    }
}
