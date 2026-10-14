package com.juguito.juguitoreader.ui.library

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.BookCriteria
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.book.ImportBookFromUriUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import com.juguito.juguitoreader.domain.usecase.library.GetLibraryLayoutModeUseCase
import com.juguito.juguitoreader.domain.usecase.library.SaveLibraryLayoutModeUseCase
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val getLibraryLayoutModeUseCase = mockk<GetLibraryLayoutModeUseCase>()
    private val saveLibraryLayoutModeUseCase = mockk<SaveLibraryLayoutModeUseCase>()
    private val layoutMode = MutableStateFlow(0)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getFoldersUseCase() } returns flowOf(emptyList())
        every { getBooksUseCase() } returns flowOf(emptyList())
        every { getLibraryLayoutModeUseCase() } returns layoutMode
        coEvery { saveLibraryLayoutModeUseCase(any()) } coAnswers {
            layoutMode.value = firstArg()
        }

        viewModel = createViewModel()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = LibraryViewModel(
        context,
        getBooksUseCase,
        getFoldersUseCase,
        updateBookUseCase,
        importBookFromUriUseCase,
        getLibraryLayoutModeUseCase,
        saveLibraryLayoutModeUseCase,
        savedStateHandle,
    )

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
        viewModel = createViewModel()

        viewModel.onEvent(LibraryEvent.OnSearchTextChanged("Android"))

        viewModel.uiState.test {
            val state = awaitItem()
            val successState = state as LibraryUiState.Success
            assertThat(successState.filteredBooks).hasSize(1)
            assertThat(successState.filteredBooks[0].title).isEqualTo("Android")
        }
    }

    @Test
    fun `onEvent OnCriteriaChanged keeps only favourite books`() = runTest {
        val books = listOf(
            Book(id = 1, title = "Fav", author = "A", isPhysical = false, isFavourite = true),
            Book(id = 2, title = "Other", author = "B", isPhysical = false)
        )
        every { getBooksUseCase() } returns flowOf(books)
        viewModel = createViewModel()

        viewModel.onEvent(LibraryEvent.OnCriteriaChanged(BookCriteria(favouritesOnly = true)))

        viewModel.uiState.test {
            val successState = awaitItem() as LibraryUiState.Success
            assertThat(successState.criteria.favouritesOnly).isTrue()
            assertThat(successState.filteredBooks.map { it.id }).containsExactly(1)
        }
    }

    @Test
    fun `onEvent OnSelectedFolderChanged updates state`() = runTest {
        val folder = Folder(id = 1, name = "My Folder", colorHex = "#000")
        every { getFoldersUseCase() } returns flowOf(listOf(folder))
        viewModel = createViewModel()

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
        
        viewModel = createViewModel()
        
        viewModel.onEvent(LibraryEvent.OnStatusChanged(1, BookStatus.READING))
        
        coVerify { updateBookUseCase(match { it.id == 1 && it.status == BookStatus.READING }) }
    }

    @Test
    fun `onEvent OnCriteriaChanged keeps only books with that status`() = runTest {
        val books = listOf(
            Book(id = 1, title = "Now", author = "A", isPhysical = false, status = BookStatus.READING),
            Book(id = 2, title = "Later", author = "B", isPhysical = false, status = BookStatus.PENDING)
        )
        every { getBooksUseCase() } returns flowOf(books)
        viewModel = createViewModel()

        viewModel.onEvent(LibraryEvent.OnCriteriaChanged(BookCriteria(statuses = setOf(BookStatus.READING))))

        viewModel.uiState.test {
            val successState = awaitItem() as LibraryUiState.Success
            assertThat(successState.criteria.statuses).containsExactly(BookStatus.READING)
            assertThat(successState.filteredBooks.map { it.id }).containsExactly(1)
        }
    }

    @Test
    fun `home nav args seed criteria and filter the shelf`() = runTest {
        val books = listOf(
            Book(id = 1, title = "Now", author = "A", isPhysical = false, status = BookStatus.READING, isFavourite = true),
            Book(id = 2, title = "Later", author = "B", isPhysical = false, status = BookStatus.PENDING, isFavourite = true),
            Book(id = 3, title = "Other", author = "C", isPhysical = false, status = BookStatus.PENDING)
        )
        every { getBooksUseCase() } returns flowOf(books)
        viewModel = createViewModel(
            SavedStateHandle(mapOf("favourites" to true, "status" to BookStatus.PENDING.name))
        )

        viewModel.uiState.test {
            val successState = awaitItem() as LibraryUiState.Success
            assertThat(successState.criteria.favouritesOnly).isTrue()
            assertThat(successState.criteria.statuses).containsExactly(BookStatus.PENDING)
            assertThat(successState.filteredBooks.map { it.id }).containsExactly(2)
        }
    }

    @Test
    fun `criteria change keeps the selected folder`() = runTest {
        val folder = Folder(id = 1, name = "Shelf", colorHex = "#000")
        val books = listOf(
            Book(id = 1, title = "In", author = "A", isPhysical = false, isFavourite = true, folders = listOf(folder)),
            Book(id = 2, title = "Out", author = "B", isPhysical = false, isFavourite = true)
        )
        every { getFoldersUseCase() } returns flowOf(listOf(folder))
        every { getBooksUseCase() } returns flowOf(books)
        viewModel = createViewModel()

        viewModel.onEvent(LibraryEvent.OnSelectedFolderChanged(folder))
        viewModel.onEvent(LibraryEvent.OnCriteriaChanged(BookCriteria(favouritesOnly = true)))

        viewModel.uiState.test {
            val successState = awaitItem() as LibraryUiState.Success
            assertThat(successState.selectedFolder).isEqualTo(folder)
            assertThat(successState.criteria.favouritesOnly).isTrue()
            assertThat(successState.filteredBooks.map { it.id }).containsExactly(1)
        }
    }

    @Test
    fun `layout mode comes from settings and survives a filter change`() = runTest {
        val books = listOf(
            Book(id = 1, title = "Fav", author = "A", isPhysical = false, isFavourite = true),
            Book(id = 2, title = "Other", author = "B", isPhysical = false)
        )
        every { getBooksUseCase() } returns flowOf(books)
        layoutMode.value = 1
        viewModel = createViewModel()

        viewModel.onEvent(LibraryEvent.OnCriteriaChanged(BookCriteria(favouritesOnly = true)))

        viewModel.uiState.test {
            val successState = awaitItem() as LibraryUiState.Success
            assertThat(successState.layoutMode).isEqualTo(1)
            assertThat(successState.filteredBooks.map { it.id }).containsExactly(1)
        }
    }

    @Test
    fun `onEvent OnLayoutModeChanged saves the mode`() = runTest {
        val books = listOf(
            Book(id = 1, title = "T", author = "A", isPhysical = false)
        )
        every { getBooksUseCase() } returns flowOf(books)
        viewModel = createViewModel()

        viewModel.onEvent(LibraryEvent.OnLayoutModeChanged(1))

        viewModel.uiState.test {
            val successState = awaitItem() as LibraryUiState.Success
            assertThat(successState.layoutMode).isEqualTo(1)
        }
        coVerify { saveLibraryLayoutModeUseCase(1) }
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
