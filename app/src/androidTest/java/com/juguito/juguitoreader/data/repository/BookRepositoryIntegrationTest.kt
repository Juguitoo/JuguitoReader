package com.juguito.juguitoreader.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase
import com.juguito.juguitoreader.data.local.entity.BookEntity
import com.juguito.juguitoreader.data.local.entity.BookFolderCrossRef
import com.juguito.juguitoreader.data.local.entity.BookGenreCrossRef
import com.juguito.juguitoreader.data.local.entity.DailyReadingEntity
import com.juguito.juguitoreader.data.local.entity.FolderEntity
import com.juguito.juguitoreader.data.local.entity.GenreEntity
import com.juguito.juguitoreader.data.local.entity.ReadingProgressEntity
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookRepositoryIntegrationTest {

    private lateinit var database: JuguitoReaderDatabase
    private lateinit var repository: BookRepositoryImpl

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            JuguitoReaderDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = BookRepositoryImpl(
            bookDAO = database.bookDAO,
            readingProgressDAO = database.readingProgressDAO,
            dailyReadingDAO = database.dailyReadingDAO,
            database = database
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun updateBookWithNewEpub_updatesRelationsAndDeletesReadingData() = runTest {
        seedBook()
        val updatedBook = Book(
            id = 1,
            title = "Updated",
            author = "Author",
            isPhysical = false,
            status = BookStatus.FINISHED,
            localFilePath = "new.epub",
            createdAt = 0L
        )

        repository.updateBookWithNewEpub(
            book = updatedBook,
            folderIds = listOf(2),
            genreIds = listOf(2)
        )

        val storedBook = database.bookDAO.getBookById(1)
        assertThat(storedBook?.book?.title).isEqualTo("Updated")
        assertThat(storedBook?.book?.status).isEqualTo(BookStatus.FINISHED)
        assertThat(storedBook?.book?.localFilePath).isEqualTo("new.epub")
        assertThat(storedBook?.folders?.map { it.id }).containsExactly(2)
        assertThat(storedBook?.genres?.map { it.id }).containsExactly(2)
        assertThat(database.readingProgressDAO.getReadingProgressById(1)).isNull()
        assertThat(database.dailyReadingDAO.getAllBookDailyReadings(1).first()).isEmpty()
    }

    @Test
    fun updateBookWithNewEpub_rollsBackAllChangesWhenRelationSyncFails() = runTest {
        seedBook()
        val updatedBook = Book(
            id = 1,
            title = "Updated",
            author = "Author",
            isPhysical = false,
            status = BookStatus.FINISHED,
            localFilePath = "new.epub",
            createdAt = 0L
        )

        val result = runCatching {
            repository.updateBookWithNewEpub(
                book = updatedBook,
                folderIds = listOf(999),
                genreIds = listOf(2)
            )
        }

        assertThat(result.isFailure).isTrue()
        val storedBook = database.bookDAO.getBookById(1)
        assertThat(storedBook?.book?.title).isEqualTo("Original")
        assertThat(storedBook?.book?.localFilePath).isEqualTo("old.epub")
        assertThat(storedBook?.folders?.map { it.id }).containsExactly(1)
        assertThat(storedBook?.genres?.map { it.id }).containsExactly(1)
        assertThat(database.readingProgressDAO.getReadingProgressById(1)).isNotNull()
        assertThat(database.dailyReadingDAO.getAllBookDailyReadings(1).first()).hasSize(1)
    }

    private suspend fun seedBook() {
        database.bookDAO.insertBook(
            BookEntity(
                id = 1,
                title = "Original",
                author = "Author",
                isPhysical = false,
                status = BookStatus.FINISHED,
                localFilePath = "old.epub",
                createdAt = 0L
            )
        )
        database.folderDAO.insertFolder(
            FolderEntity(id = 1, name = "Old folder", colorHex = "#000000", createdAt = 0L)
        )
        database.folderDAO.insertFolder(
            FolderEntity(id = 2, name = "New folder", colorHex = "#FFFFFF", createdAt = 0L)
        )
        database.genreDAO.insertGenre(
            GenreEntity(id = 1, name = "Old genre", createdAt = 0L)
        )
        database.genreDAO.insertGenre(
            GenreEntity(id = 2, name = "New genre", createdAt = 0L)
        )
        database.bookDAO.insertBookFolderCrossRefs(
            listOf(BookFolderCrossRef(bookId = 1, folderId = 1))
        )
        database.bookDAO.insertBookGenreCrossRefs(
            listOf(BookGenreCrossRef(bookId = 1, genreId = 1))
        )
        database.readingProgressDAO.insertReadingProgress(
            ReadingProgressEntity(
                bookId = 1,
                totalChapters = 10,
                lastChapterIndex = 9,
                scrollPosition = 0.5f,
                lastReadAt = 0L
            )
        )
        database.dailyReadingDAO.insert(
            DailyReadingEntity(
                bookId = 1,
                date = "2026-08-30",
                timeSpentMillis = 60_000,
                reachedPercentage = 95f,
                readingSpeed = 200
            )
        )
    }
}
