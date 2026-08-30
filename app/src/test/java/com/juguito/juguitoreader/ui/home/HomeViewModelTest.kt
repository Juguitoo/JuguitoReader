package com.juguito.juguitoreader.ui.home

import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.usecase.book.AddBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.DeleteBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.book.ImportBookFromUriUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.GetReadingProgressesUseCase
import com.juguito.juguitoreader.utils.FileUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var viewModel: HomeViewModel
    private val context = mockk<Context>(relaxed = true)
    private val getBooksUseCase = mockk<GetBooksUseCase>()
    private val getReadingProgressesUseCase = mockk<GetReadingProgressesUseCase>()
    private val importBookFromUriUseCase = mockk<ImportBookFromUriUseCase>()
    private val deleteBookUseCase = mockk<DeleteBookUseCase>()
    private val addBookUseCase = mockk<AddBookUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getBooksUseCase() } returns flowOf(emptyList())
        every { getReadingProgressesUseCase() } returns flowOf(emptyList())
        
        viewModel = HomeViewModel(
            context,
            getBooksUseCase,
            getReadingProgressesUseCase,
            importBookFromUriUseCase,
            deleteBookUseCase,
            addBookUseCase
        )
        mockkObject(FileUtils)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Success state shows stats and filtered books`() = runTest {
        val books = listOf(
            Book(id = 1, title = "Book 1", author = "A", isPhysical = false, localFilePath = "path", status = BookStatus.PENDING),
            Book(id = 2, title = "Book 2", author = "B", isPhysical = false, localFilePath = "path", status = BookStatus.READING)
        )
        every { getBooksUseCase() } returns flowOf(books)
        
        viewModel = HomeViewModel(context, getBooksUseCase, getReadingProgressesUseCase, importBookFromUriUseCase, deleteBookUseCase, addBookUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(HomeUiState.Success::class.java)
            val successState = state as HomeUiState.Success
            assertThat(successState.stats.totalBooksCount).isEqualTo(2)
            assertThat(successState.pendingBooks.map { it.id }).containsExactly(1)
            assertThat(successState.readingBooks.map { it.id }).containsExactly(2)
        }
    }

    @Test
    fun `reading book without progress remains visible after EPUB replacement`() = runTest {
        val readingBook = Book(
            id = 1,
            title = "Book",
            author = "Author",
            isPhysical = false,
            localFilePath = "new.epub",
            status = BookStatus.READING
        )
        every { getBooksUseCase() } returns flowOf(listOf(readingBook))
        every { getReadingProgressesUseCase() } returns flowOf(emptyList())

        viewModel = HomeViewModel(
            context,
            getBooksUseCase,
            getReadingProgressesUseCase,
            importBookFromUriUseCase,
            deleteBookUseCase,
            addBookUseCase
        )

        viewModel.uiState.test {
            val state = awaitItem() as HomeUiState.Success
            assertThat(state.readingBooks.map { it.id }).containsExactly(1)
            assertThat(state.pendingBooks).isEmpty()
        }
    }

    @Test
    fun `onEvent OnDeleteBookClick and then OnUndoDeleteClick calls addBookUseCase`() = runTest {
        val book = Book(id = 1, title = "T", author = "A", isPhysical = false)
        coEvery { deleteBookUseCase(1) } returns Result.success(Unit)
        coEvery { addBookUseCase(any()) } returns Result.success(Unit)
        
        viewModel.onEvent(HomeEvent.OnDeleteBookClick(book))
        viewModel.onEvent(HomeEvent.OnUndoDeleteClick)
        
        coVerify { deleteBookUseCase(1) }
        coVerify { addBookUseCase(book) }
    }

    @Test
    fun `onEvent OnDeleteConfirmed calls FileUtils delete`() = runTest {
        every { FileUtils.deleteFileFromInternalStorage(any(), any()) } returns Unit
        
        val book = Book(id = 1, title = "T", author = "A", isPhysical = false, coverUrl = "c", localFilePath = "l")
        coEvery { deleteBookUseCase(1) } returns Result.success(Unit)
        
        viewModel.onEvent(HomeEvent.OnDeleteBookClick(book))
        viewModel.onEvent(HomeEvent.OnDeleteConfirmed)
        
        coVerify { FileUtils.deleteFileFromInternalStorage(any(), "c") }
        coVerify { FileUtils.deleteFileFromInternalStorage(any(), "l") }
    }

    @Test
    fun `onEvent OnImportBook calls use case`() = runTest {
        val uri = mockk<Uri>()
        coEvery { importBookFromUriUseCase(any(), any()) } returns Result.success(Unit)
        
        viewModel.onEvent(HomeEvent.OnImportBook(uri))
        
        coVerify { importBookFromUriUseCase(any(), any()) }
    }
}
