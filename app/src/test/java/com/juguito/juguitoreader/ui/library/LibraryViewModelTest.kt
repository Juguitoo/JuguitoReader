package com.juguito.juguitoreader.ui.library

import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.book.ImportBookFromUriUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
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
class LibraryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var viewModel: LibraryViewModel
    private val context = mockk<Context>()
    private val getBooksUseCase = mockk<GetBooksUseCase>()
    private val getFoldersUseCase = mockk<GetFoldersUseCase>()
    private val updateBookUseCase = mockk<UpdateBookUseCase>()
    private val importBookFromUriUseCase = mockk<ImportBookFromUriUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getFoldersUseCase() } returns flowOf(emptyList())
        every { getBooksUseCase() } returns flowOf(emptyList())
        
        viewModel = LibraryViewModel(
            context,
            getBooksUseCase,
            getFoldersUseCase,
            updateBookUseCase,
            importBookFromUriUseCase
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Empty if no books`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(LibraryUiState.Empty::class.java)
        }
    }

    @Test
    fun `onEvent OnSearchTextChanged filters books in Success state`() = runTest {
        val books = listOf(
            Book(id = 1, title = "Android", author = "A", isPhysical = false),
            Book(id = 2, title = "Kotlin", author = "B", isPhysical = false)
        )
        every { getBooksUseCase() } returns flowOf(books)
        viewModel = LibraryViewModel(context, getBooksUseCase, getFoldersUseCase, updateBookUseCase, importBookFromUriUseCase)

        viewModel.onEvent(LibraryEvent.OnSearchTextChanged("Android"))

        viewModel.uiState.test {
            val state = awaitItem()
            val successState = state as LibraryUiState.Success
            assertThat(successState.filteredBooks).hasSize(1)
            assertThat(successState.filteredBooks[0].title).isEqualTo("Android")
        }
    }

    @Test
    fun `onEvent OnSelectedFolderChanged updates state`() = runTest {
        val folder = Folder(id = 1, name = "My Folder", colorHex = "#000")
        every { getFoldersUseCase() } returns flowOf(listOf(folder))
        viewModel = LibraryViewModel(context, getBooksUseCase, getFoldersUseCase, updateBookUseCase, importBookFromUriUseCase)

        viewModel.onEvent(LibraryEvent.OnSelectedFolderChanged(folder))

        viewModel.uiState.test {
            val state = awaitItem()
            val emptyState = state as LibraryUiState.Empty
            assertThat(emptyState.selectedFolder).isEqualTo(folder)
        }
    }

    @Test
    fun `onEvent OnStatusChanged calls updateBookUseCase`() = runTest {
        val book = Book(id = 1, title = "T", author = "A", isPhysical = false, status = BookStatus.PENDING)
        every { getBooksUseCase() } returns flowOf(listOf(book))
        coEvery { updateBookUseCase(any()) } returns Result.success(Unit)
        
        viewModel = LibraryViewModel(context, getBooksUseCase, getFoldersUseCase, updateBookUseCase, importBookFromUriUseCase)
        
        viewModel.onEvent(LibraryEvent.OnStatusChanged(1, BookStatus.READING))
        
        coVerify { updateBookUseCase(match { it.id == 1 && it.status == BookStatus.READING }) }
    }

    @Test
    fun `importBook calls use case and handles error`() = runTest {
        val uri = mockk<Uri>()
        coEvery { importBookFromUriUseCase(any(), any()) } returns Result.failure(Exception("Error"))
        
        viewModel.importBook(uri)
        
        viewModel.effect.test {
            val effect = awaitItem()
            assertThat(effect).isInstanceOf(UiEffect.ShowSnackbar::class.java)
        }
    }

    @Test
    fun `FILE-015 importBook twice calls use case once`() = runTest {
        val uri = mockk<Uri>()
        val latch = CompletableDeferred<Result<Unit>>()
        coEvery { importBookFromUriUseCase(any(), any()) } coAnswers { latch.await() }

        try {
            viewModel.importBook(uri)
            viewModel.importBook(uri)

            coVerify(exactly = 1) { importBookFromUriUseCase(any(), any()) }
        } finally {
            latch.complete(Result.success(Unit))
        }
    }

    @Test
    fun `dismissError reloads data`() = runTest {
        viewModel.dismissError()
        // data is loaded in init and in loadData, verifying no crash and flow continues
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(LibraryUiState.Empty::class.java)
        }
    }
}
