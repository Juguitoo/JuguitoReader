package com.juguito.juguitoreader.domain.usecase.book

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.domain.repository.FolderRepository
import com.juguito.juguitoreader.domain.repository.GenreRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateBookUseCaseTest {

    private lateinit var useCase: UpdateBookUseCase
    private val bookRepository = mockk<BookRepository>()
    private val folderRepository = mockk<FolderRepository>()
    private val genreRepository = mockk<GenreRepository>()

    @Before
    fun setup() {
        useCase = UpdateBookUseCase(bookRepository, folderRepository, genreRepository)
        coEvery { bookRepository.getBookById(any()) } returns Book(
            id = 1,
            title = "Persisted",
            author = "Author",
            isPhysical = true
        )
    }

    @Test
    fun `invoke with blank title returns failure`() = runTest {
        val result = useCase(Book(id = 1, title = "", author = "Author", isPhysical = true))
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_title_empty)
    }

    @Test
    fun `invoke with blank author returns failure`() = runTest {
        val result = useCase(Book(id = 1, title = "Title", author = "", isPhysical = true))
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_author_empty)
    }

    @Test
    fun `invoke with valid book calls updateBookWithCrossRefs and returns success`() = runTest {
        val book = Book(id = 1, title = "Title", author = "Author", isPhysical = true)
        coEvery { bookRepository.updateBookWithCrossRefs(any(), any(), any()) } returns Unit

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify { bookRepository.updateBookWithCrossRefs(book, emptyList(), emptyList()) }
    }

    @Test
    fun `invoke with existing folder and genre id uses resolved ids in updateBookWithCrossRefs`() = runBlocking {
        val folder = Folder(id = 5, name = "Sci-Fi-OLD", colorHex = "#FFF")
        val genre = Genre(id = 3, name = "Fairy")
        val book = Book(id = 1, title = "T", author = "A", isPhysical = true, folders = listOf(folder), genres = listOf(genre))

        coEvery { folderRepository.getFolderById(5) } returns Folder(id = 5, name = "Science Fiction", colorHex = "#FFF")
        coEvery { genreRepository.getGenreById(3) } returns Genre(id = 3, name = "Fantasy")
        coEvery { bookRepository.updateBookWithCrossRefs(any(), any(), any()) } returns Unit

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify { bookRepository.updateBookWithCrossRefs(book, listOf(5), listOf(3)) }
        coVerify(exactly = 0) { folderRepository.getFolderByName(any()) }
        coVerify(exactly = 0) { folderRepository.insertFolder(any()) }
        coVerify(exactly = 0) { genreRepository.getGenreByName(any()) }
        coVerify(exactly = 0) { genreRepository.insertGenre(any()) }
    }

    @Test
    fun `invoke with non existing folder and genre calls updateBookWithCrossRefs with new ids`() = runBlocking {
        val folder = Folder(id = 0, name = "Sci-Fi", colorHex = "#FFF")
        val genre = Genre(id = 0, name = "Fantasy")
        val book = Book(id = 1, title = "T", author = "A", isPhysical = true, folders = listOf(folder), genres = listOf(genre))

        coEvery { folderRepository.getFolderByName(any()) } returns null
        coEvery { genreRepository.getGenreByName(any()) } returns null
        coEvery { folderRepository.insertFolder(any()) } returns 5L
        coEvery { genreRepository.insertGenre(any()) } returns 20L
        coEvery { bookRepository.updateBookWithCrossRefs(any(), any(), any()) } returns Unit

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify { bookRepository.updateBookWithCrossRefs(book, listOf(5), listOf(20)) }
        coVerify(exactly = 1) { folderRepository.getFolderByName("Sci-Fi") }
        coVerify(exactly = 1) { folderRepository.insertFolder(any()) }
        coVerify(exactly = 1) { genreRepository.getGenreByName("Fantasy") }
        coVerify(exactly = 1) { genreRepository.insertGenre(any()) }
    }

    @Test
    fun `invoke with existing folder and genre by name calls updateBookWithCrossRefs with resolved ids`() = runBlocking {
        val folder = Folder(id = 0, name = "Sci-Fi", colorHex = "#FFF")
        val genre = Genre(id = 0, name = "Fantasy")
        val book = Book(id = 1, title = "T", author = "A", isPhysical = true, folders = listOf(folder), genres = listOf(genre))

        coEvery { folderRepository.getFolderByName(any()) } returns Folder(id = 5, name = "Sci-Fi", colorHex = "#FFF")
        coEvery { genreRepository.getGenreByName(any()) } returns Genre(id = 20, name = "Fantasy")
        coEvery { bookRepository.updateBookWithCrossRefs(any(), any(), any()) } returns Unit

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify { bookRepository.updateBookWithCrossRefs(book, listOf(5), listOf(20)) }
        coVerify(exactly = 1) { folderRepository.getFolderByName("Sci-Fi") }
        coVerify(exactly = 0) { folderRepository.insertFolder(any()) }
        coVerify(exactly = 1) { genreRepository.getGenreByName("Fantasy") }
        coVerify(exactly = 0) { genreRepository.insertGenre(any()) }
    }

    @Test
    fun `invoke with changed EPUB resets reading data with resolved relation ids`() = runTest {
        val folder = Folder(id = 0, name = "Sci-Fi", colorHex = "#FFF")
        val genre = Genre(id = 0, name = "Fantasy")
        val book = Book(
            id = 1,
            title = "T",
            author = "A",
            isPhysical = false,
            localFilePath = "new.epub",
            folders = listOf(folder),
            genres = listOf(genre)
        )
        coEvery { folderRepository.getFolderByName("Sci-Fi") } returns null
        coEvery { folderRepository.insertFolder(any()) } returns 5L
        coEvery { genreRepository.getGenreByName("Fantasy") } returns null
        coEvery { genreRepository.insertGenre(any()) } returns 20L
        coEvery { bookRepository.updateBookWithNewEpub(any(), any(), any()) } returns Unit

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) {
            bookRepository.updateBookWithNewEpub(book, listOf(5), listOf(20))
        }
        coVerify(exactly = 0) { bookRepository.updateBookWithCrossRefs(any(), any(), any()) }
        coVerify(exactly = 0) { bookRepository.updateBook(any()) }
        coVerify(exactly = 0) { bookRepository.syncCrossReferences(any(), any(), any()) }
    }

    @Test
    fun `invoke with physical leftover epub strips path without resetting reading data`() = runTest {
        coEvery { bookRepository.getBookById(1) } returns Book(
            id = 1,
            title = "Persisted",
            author = "Author",
            isPhysical = false,
            localFilePath = "old.epub",
        )
        val book = Book(
            id = 1,
            title = "T",
            author = "A",
            isPhysical = true,
            localFilePath = "old.epub",
        )
        coEvery { bookRepository.updateBookWithCrossRefs(any(), any(), any()) } returns Unit

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify {
            bookRepository.updateBookWithCrossRefs(
                match { it.isPhysical && it.localFilePath == null },
                emptyList(),
                emptyList(),
            )
        }
        coVerify(exactly = 0) { bookRepository.updateBookWithNewEpub(any(), any(), any()) }
    }

    @Test
    fun `invoke clearing digital epub does not reset reading data`() = runTest {
        coEvery { bookRepository.getBookById(1) } returns Book(
            id = 1,
            title = "Persisted",
            author = "Author",
            isPhysical = false,
            localFilePath = "old.epub",
        )
        val book = Book(
            id = 1,
            title = "T",
            author = "A",
            isPhysical = false,
            localFilePath = null,
        )
        coEvery { bookRepository.updateBookWithCrossRefs(any(), any(), any()) } returns Unit

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify { bookRepository.updateBookWithCrossRefs(book, emptyList(), emptyList()) }
        coVerify(exactly = 0) { bookRepository.updateBookWithNewEpub(any(), any(), any()) }
    }

    @Test
    fun `invoke returns failure when persisted book does not exist`() = runTest {
        coEvery { bookRepository.getBookById(1) } returns null

        val result = useCase(Book(id = 1, title = "T", author = "A", isPhysical = true))

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_save_book)
    }
}
