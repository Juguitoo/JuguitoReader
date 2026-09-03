package com.juguito.juguitoreader.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase
import com.juguito.juguitoreader.data.local.entity.BookEntity
import com.juguito.juguitoreader.data.local.entity.BookFolderCrossRef
import com.juguito.juguitoreader.data.local.entity.BookGenreCrossRef
import com.juguito.juguitoreader.data.local.entity.FolderEntity
import com.juguito.juguitoreader.data.local.entity.GenreEntity
import com.juguito.juguitoreader.data.local.entity.ReadingProgressEntity
import com.juguito.juguitoreader.domain.enums.BookStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookDAOTest {

    private lateinit var database: JuguitoReaderDatabase
    private lateinit var bookDAO: BookDAO

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            JuguitoReaderDatabase::class.java
        ).allowMainThreadQueries().build()
        bookDAO = database.bookDAO
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertBook_returns_id() = runBlocking {
        val book = BookEntity(
            title = "Test Book",
            author = "Test Author",
            isPhysical = false,
            status = BookStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        val id = bookDAO.insertBook(book)
        assertThat(id).isGreaterThan(0)
    }

    @Test
    fun getBookById_returns_correct_book_with_details() = runBlocking {
        val book = BookEntity(id = 1, title = "T", author = "A", isPhysical = false, createdAt = 0L)
        val genre = GenreEntity(id = 1, name = "G", createdAt = 0L)
        val folder = FolderEntity(id = 1, name = "F", colorHex = "#000", createdAt = 0L)
        
        bookDAO.insertBook(book)
        database.genreDAO.insertGenre(genre)
        database.folderDAO.insertFolder(folder)
        bookDAO.insertBookGenreCrossRefs(listOf(BookGenreCrossRef(1, 1)))
        bookDAO.insertBookFolderCrossRefs(listOf(BookFolderCrossRef(1, 1)))
        
        val result = bookDAO.getBookById(1)
        assertThat(result).isNotNull()
        assertThat(result?.book?.title).isEqualTo("T")
        assertThat(result?.genres).hasSize(1)
        assertThat(result?.folders).hasSize(1)
    }

    @Test
    fun deleteBookById_removes_book() = runBlocking {
        val book = BookEntity(id = 1, title = "Delete", author = "A", isPhysical = false, createdAt = 0L)
        bookDAO.insertBook(book)
        bookDAO.deleteBookById(1)
        
        val result = bookDAO.getBookById(1)
        assertThat(result).isNull()
    }

    @Test
    fun deleteBookById_cascades_reading_progress() = runBlocking {
        val book = BookEntity(id = 1, title = "Delete", author = "A", isPhysical = false, createdAt = 0L)
        bookDAO.insertBook(book)
        database.readingProgressDAO.insertReadingProgress(
            ReadingProgressEntity(
                bookId = 1,
                totalChapters = 20,
                lastChapterIndex = 5,
                scrollPosition = 0.75f,
                lastReadAt = 100L
            )
        )

        bookDAO.deleteBookById(1)

        assertThat(database.readingProgressDAO.getReadingProgressById(1)).isNull()
    }

    @Test
    fun getAllBooks_returns_flow_of_books() = runBlocking {
        val book = BookEntity(title = "B", author = "A", isPhysical = false, createdAt = 0L)
        bookDAO.insertBook(book)
        
        val books = bookDAO.getAllBooks().first()
        assertThat(books).isNotEmpty()
        assertThat(books[0].book.title).isEqualTo("B")
    }

    @Test
    fun updateBook_preservesFoldersAndGenres() = runBlocking {
        val book = BookEntity(id = 1, title = "Original", author = "A", isPhysical = false, createdAt = 0L)
        val folder = FolderEntity(id = 1, name = "F", colorHex = "#000", createdAt = 0L)
        val genre = GenreEntity(id = 1, name = "G", createdAt = 0L)

        bookDAO.insertBook(book)
        database.folderDAO.insertFolder(folder)
        database.genreDAO.insertGenre(genre)
        bookDAO.insertBookFolderCrossRefs(listOf(BookFolderCrossRef(bookId = 1, folderId = 1)))
        bookDAO.insertBookGenreCrossRefs(listOf(BookGenreCrossRef(bookId = 1, genreId = 1)))

        bookDAO.updateBook(book.copy(title = "Renamed", status = BookStatus.READING))

        val result = bookDAO.getBookById(1)
        assertThat(result).isNotNull()
        assertThat(result?.book?.title).isEqualTo("Renamed")
        assertThat(result?.book?.status).isEqualTo(BookStatus.READING)
        assertThat(result?.folders).hasSize(1)
        assertThat(result?.folders?.first()?.id).isEqualTo(1)
        assertThat(result?.genres).hasSize(1)
        assertThat(result?.genres?.first()?.id).isEqualTo(1)
    }

    @Test
    fun syncBookCrossRefs_removesStaleFolderAndGenre() = runBlocking {
        seedBookWithFoldersAndGenres(
            folderIds = listOf(1, 2),
            genreIds = listOf(1, 2),
        )

        bookDAO.syncBookCrossRefs(bookId = 1, folderIds = listOf(1), genreIds = listOf(1))

        val result = bookDAO.getBookById(1)
        assertThat(result?.folders).hasSize(1)
        assertThat(result?.folders?.first()?.id).isEqualTo(1)
        assertThat(result?.genres).hasSize(1)
        assertThat(result?.genres?.first()?.id).isEqualTo(1)
    }

    @Test
    fun syncBookCrossRefs_clearsAllWhenListsEmpty() = runBlocking {
        seedBookWithFoldersAndGenres(
            folderIds = listOf(1),
            genreIds = listOf(1),
        )

        bookDAO.syncBookCrossRefs(bookId = 1, folderIds = emptyList(), genreIds = emptyList())

        val result = bookDAO.getBookById(1)
        assertThat(result?.folders).isEmpty()
        assertThat(result?.genres).isEmpty()
    }

    @Test
    fun syncBookCrossRefs_replacesFullSet() = runBlocking {
        seedBookWithFoldersAndGenres(
            folderIds = listOf(1),
            genreIds = listOf(1),
        )
        database.folderDAO.insertFolder(FolderEntity(id = 2, name = "F2", colorHex = "#000", createdAt = 0L))
        database.genreDAO.insertGenre(GenreEntity(id = 2, name = "G2", createdAt = 0L))

        bookDAO.syncBookCrossRefs(bookId = 1, folderIds = listOf(2), genreIds = listOf(2))

        val result = bookDAO.getBookById(1)
        assertThat(result?.folders).hasSize(1)
        assertThat(result?.folders?.first()?.id).isEqualTo(2)
        assertThat(result?.genres).hasSize(1)
        assertThat(result?.genres?.first()?.id).isEqualTo(2)
    }

    private suspend fun seedBookWithFoldersAndGenres(
        folderIds: List<Int>,
        genreIds: List<Int>,
    ) {
        val book = BookEntity(id = 1, title = "T", author = "A", isPhysical = false, createdAt = 0L)
        bookDAO.insertBook(book)

        folderIds.distinct().forEach { folderId ->
            database.folderDAO.insertFolder(
                FolderEntity(id = folderId, name = "F$folderId", colorHex = "#000", createdAt = 0L)
            )
        }
        genreIds.distinct().forEach { genreId ->
            database.genreDAO.insertGenre(
                GenreEntity(id = genreId, name = "G$genreId", createdAt = 0L)
            )
        }

        if (folderIds.isNotEmpty()) {
            bookDAO.insertBookFolderCrossRefs(folderIds.map { BookFolderCrossRef(bookId = 1, folderId = it) })
        }
        if (genreIds.isNotEmpty()) {
            bookDAO.insertBookGenreCrossRefs(genreIds.map { BookGenreCrossRef(bookId = 1, genreId = it) })
        }
    }
}
