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
import org.junit.Before
import org.junit.Test

class AddBookUseCaseTest {

    private lateinit var useCase: AddBookUseCase
    private val bookRepository = mockk<BookRepository>()
    private val folderRepository = mockk<FolderRepository>()
    private val genreRepository = mockk<GenreRepository>()

    @Before
    fun setup() {
        useCase = AddBookUseCase(bookRepository, folderRepository, genreRepository)
    }

    @Test
    fun `invoke with empty title returns failure`() = runBlocking {
        val book = Book(title = "", author = "Author", isPhysical = false)
        val result = useCase(book)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_title_empty)
    }

    @Test
    fun `invoke with empty author returns failure`() = runBlocking {
        val book = Book(title = "Title", author = "", isPhysical = false)
        val result = useCase(book)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_author_empty)
    }

    @Test
    fun `invoke with valid book calls insertBookWithCrossRefs and returns success`() = runBlocking {
        val book = Book(title = "Valid Title", author = "Valid Author", isPhysical = false)

        coEvery { bookRepository.insertBookWithCrossRefs(any(), any(), any()) } returns 1L

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { bookRepository.insertBookWithCrossRefs(book, emptyList(), emptyList()) }
    }

    @Test
    fun `invoke with exception in repository returns failure`() = runBlocking {
        val book = Book(title = "Title", author = "Author", isPhysical = false)
        coEvery { bookRepository.insertBookWithCrossRefs(any(), any(), any()) } throws Exception("DB Error")

        val result = useCase(book)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_save_book)
    }

    @Test
    fun `invoke with existing folder and genre id uses resolved ids in insertBookWithCrossRefs`() = runBlocking {
        val folder = Folder(id = 5, name = "Sci-Fi-OLD", colorHex = "#FFF")
        val genre = Genre(id = 3, name = "Fairy")
        val book = Book(id = 1, title = "T", author = "A", isPhysical = true, folders = listOf(folder), genres = listOf(genre))

        coEvery { folderRepository.getFolderById(5) } returns Folder(id = 5, name = "Science Fiction", colorHex = "#FFF")
        coEvery { genreRepository.getGenreById(3) } returns Genre(id = 3, name = "Fantasy")
        coEvery { bookRepository.insertBookWithCrossRefs(any(), any(), any()) } returns 1L

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { bookRepository.insertBookWithCrossRefs(book, listOf(5), listOf(3)) }
        coVerify(exactly = 0) { folderRepository.getFolderByName(any()) }
        coVerify(exactly = 0) { folderRepository.insertFolder(any()) }
        coVerify(exactly = 0) { genreRepository.getGenreByName(any()) }
        coVerify(exactly = 0) { genreRepository.insertGenre(any()) }
    }

    @Test
    fun `invoke with non existing folder and genre calls insertBookWithCrossRefs with new ids`() = runBlocking {
        val folder = Folder(id = 0, name = "Sci-Fi", colorHex = "#FFF")
        val genre = Genre(id = 0, name = "Fantasy")
        val book = Book(id = 1, title = "T", author = "A", isPhysical = true, folders = listOf(folder), genres = listOf(genre))

        coEvery { folderRepository.getFolderByName(any()) } returns null
        coEvery { genreRepository.getGenreByName(any()) } returns null
        coEvery { folderRepository.insertFolder(any()) } returns 5L
        coEvery { genreRepository.insertGenre(any()) } returns 20L
        coEvery { bookRepository.insertBookWithCrossRefs(any(), any(), any()) } returns 1L

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify { bookRepository.insertBookWithCrossRefs(book, listOf(5), listOf(20)) }
        coVerify(exactly = 1) { folderRepository.getFolderByName("Sci-Fi") }
        coVerify(exactly = 1) { folderRepository.insertFolder(any()) }
        coVerify(exactly = 1) { genreRepository.getGenreByName("Fantasy") }
        coVerify(exactly = 1) { genreRepository.insertGenre(any()) }
    }

    @Test
    fun `invoke with existing folder and genre by name calls insertBookWithCrossRefs with resolved ids`() = runBlocking {
        val folder = Folder(id = 0, name = "Sci-Fi", colorHex = "#FFF")
        val genre = Genre(id = 0, name = "Fantasy")
        val book = Book(id = 1, title = "T", author = "A", isPhysical = true, folders = listOf(folder), genres = listOf(genre))

        coEvery { folderRepository.getFolderByName(any()) } returns Folder(id = 5, name = "Sci-Fi", colorHex = "#FFF")
        coEvery { genreRepository.getGenreByName(any()) } returns Genre(id = 20, name = "Fantasy")
        coEvery { bookRepository.insertBookWithCrossRefs(any(), any(), any()) } returns 1L

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify { bookRepository.insertBookWithCrossRefs(book, listOf(5), listOf(20)) }
        coVerify(exactly = 1) { folderRepository.getFolderByName("Sci-Fi") }
        coVerify(exactly = 0) { folderRepository.insertFolder(any()) }
        coVerify(exactly = 1) { genreRepository.getGenreByName("Fantasy") }
        coVerify(exactly = 0) { genreRepository.insertGenre(any()) }
    }
}
