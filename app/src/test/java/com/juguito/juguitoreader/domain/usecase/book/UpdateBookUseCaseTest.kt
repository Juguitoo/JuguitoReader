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
    fun `invoke with valid book calls updateBook and returns success`() = runTest {
        val book = Book(id = 1, title = "Title", author = "Author", isPhysical = true)
        coEvery { bookRepository.updateBook(any()) } returns Unit
        coEvery { bookRepository.syncCrossReferences(any(), any(), any()) } returns Unit

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify { bookRepository.updateBook(book) }
        coVerify { bookRepository.syncCrossReferences(1, any(), any()) }
    }

    @Test
    fun `invoke with existing folder and genre id uses that ids in syncCrossReferences`() = runBlocking {
        val folder = Folder(id = 5, name = "Sci-Fi-OLD", colorHex = "#FFF")
        val genre = Genre(id = 3, name = "Fairy")
        val book = Book(id = 1, title = "T", author = "A", isPhysical = true, folders = listOf(folder), genres = listOf(genre))

        coEvery { folderRepository.getFolderById(5) } returns Folder(id = 5, name = "Science Fiction", colorHex = "#FFF")
        coEvery { genreRepository.getGenreById(3) } returns Genre(id = 3, name = "Fantasy")
        coEvery { bookRepository.updateBook(any()) } returns Unit
        coEvery { bookRepository.syncCrossReferences(any(), any(), any()) } returns Unit

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify { bookRepository.syncCrossReferences(1, listOf(5), listOf(3)) }
        coVerify(exactly = 0) { folderRepository.getFolderByName(any()) }
        coVerify(exactly = 0) { folderRepository.insertFolder(any()) }
        coVerify(exactly = 0) { genreRepository.getGenreByName(any()) }
        coVerify(exactly = 0) { genreRepository.insertGenre(any()) }
    }

    @Test
    fun `invoke with non existing folder and genre calls syncCrossReferences with new ids`() = runBlocking {
        val folder = Folder(id = 0, name = "Sci-Fi", colorHex = "#FFF")
        val genre = Genre(id = 0, name = "Fantasy")
        val book = Book(id = 1, title = "T", author = "A", isPhysical = true, folders = listOf(folder), genres = listOf(genre))

        coEvery { folderRepository.getFolderByName(any()) } returns null
        coEvery { genreRepository.getGenreByName(any()) } returns null
        coEvery { folderRepository.insertFolder(any()) } returns 5L
        coEvery { genreRepository.insertGenre(any()) } returns 20L
        coEvery { bookRepository.updateBook(any()) } returns Unit
        coEvery { bookRepository.syncCrossReferences(any(), any(), any()) } returns Unit

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify { bookRepository.syncCrossReferences(1, listOf(5), listOf(20)) }
        coVerify(exactly = 1) { folderRepository.getFolderByName("Sci-Fi") }
        coVerify(exactly = 1) { folderRepository.insertFolder(any()) }
        coVerify(exactly = 1) { genreRepository.getGenreByName("Fantasy") }
        coVerify(exactly = 1) { genreRepository.insertGenre(any()) }
    }

    @Test
    fun `invoke with existing folder and genre but with null id calls syncCrossReferences`() = runBlocking {
        val folder = Folder(id = 0, name = "Sci-Fi", colorHex = "#FFF")
        val genre = Genre(id = 0, name = "Fantasy")
        val book = Book(id = 1, title = "T", author = "A", isPhysical = true, folders = listOf(folder), genres = listOf(genre))

        coEvery { folderRepository.getFolderByName(any()) } returns Folder(id = 5, name = "Sci-Fi", colorHex = "#FFF")
        coEvery { genreRepository.getGenreByName(any()) } returns Genre(id = 20, name = "Fantasy")
        coEvery { folderRepository.insertFolder(any()) } returns 5L
        coEvery { genreRepository.insertGenre(any()) } returns 20L
        coEvery { bookRepository.updateBook(any()) } returns Unit
        coEvery { bookRepository.syncCrossReferences(any(), any(), any()) } returns Unit

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify { bookRepository.syncCrossReferences(1, listOf(5), listOf(20)) }
        coVerify(exactly = 1) { folderRepository.getFolderByName("Sci-Fi") }
        coVerify(exactly = 0) { folderRepository.insertFolder(any()) }
        coVerify(exactly = 1) { genreRepository.getGenreByName("Fantasy") }
        coVerify(exactly = 0) { genreRepository.insertGenre(any()) }
    }
}
