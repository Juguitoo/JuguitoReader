package com.juguito.juguitoreader.ui.home

import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.model.DeletedBookSnapshot
import com.juguito.juguitoreader.domain.usecase.book.DeleteBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.book.ImportBookFromUriUseCase
import com.juguito.juguitoreader.domain.usecase.book.RestoreDeletedBookUseCase
import com.juguito.juguitoreader.domain.usecase.dailyReading.GetBookDailyReadingsUseCase
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.usecase.readingProgress.GetReadingProgressByIdUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.GetReadingProgressesUseCase
import com.juguito.juguitoreader.ui.common.UiText
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
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
    private val getBookDailyReadingsUseCase = mockk<GetBookDailyReadingsUseCase>()
    private val getReadingProgressByIdUseCase = mockk<GetReadingProgressByIdUseCase>()
    private val importBookFromUriUseCase = mockk<ImportBookFromUriUseCase>()
    private val deleteBookUseCase = mockk<DeleteBookUseCase>()
    private val restoreDeletedBookUseCase = mockk<RestoreDeletedBookUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getBooksUseCase() } returns flowOf(emptyList())
        every { getReadingProgressesUseCase() } returns flowOf(emptyList())
        every { getBookDailyReadingsUseCase(any()) } returns flowOf(emptyList())
        coEvery { getReadingProgressByIdUseCase(any()) } returns null

        viewModel = createViewModel()
        mockkObject(FileUtils)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            context,
            getBooksUseCase,
            getReadingProgressesUseCase,
            getBookDailyReadingsUseCase,
            getReadingProgressByIdUseCase,
            importBookFromUriUseCase,
            deleteBookUseCase,
            restoreDeletedBookUseCase
        )
    }

    @Test
    fun `Success state shows stats and filtered books`() = runTest {
        val books = listOf(
            Book(id = 1, title = "Book 1", author = "A", isPhysical = false, localFilePath = "path", status = BookStatus.PENDING),
            Book(id = 2, title = "Book 2", author = "B", isPhysical = false, localFilePath = "path", status = BookStatus.READING)
        )
        every { getBooksUseCase() } returns flowOf(books)

        viewModel = createViewModel()

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

        viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem() as HomeUiState.Success
            assertThat(state.readingBooks.map { it.id }).containsExactly(1)
            assertThat(state.pendingBooks).isEmpty()
        }
    }

    @Test
    fun `empty catalog emits Empty`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(HomeUiState.Empty)
        }
    }

    @Test
    fun `physical books only emit Success with empty shelves not Empty`() = runTest {
        val books = listOf(
            Book(id = 1, title = "Paper", author = "A", isPhysical = true, status = BookStatus.READING)
        )
        every { getBooksUseCase() } returns flowOf(books)

        viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(HomeUiState.Success::class.java)
            val success = state as HomeUiState.Success
            assertThat(success.stats.totalBooksCount).isEqualTo(1)
            assertThat(success.readingBooks).isEmpty()
            assertThat(success.pendingBooks).isEmpty()
        }
    }

    @Test
    fun `digital book without EPUB is omitted from shelves`() = runTest {
        val books = listOf(
            Book(
                id = 1,
                title = "Soon",
                author = "A",
                isPhysical = false,
                localFilePath = null,
                status = BookStatus.PENDING
            )
        )
        every { getBooksUseCase() } returns flowOf(books)

        viewModel = createViewModel()

        viewModel.uiState.test {
            val success = awaitItem() as HomeUiState.Success
            assertThat(success.stats.totalBooksCount).isEqualTo(1)
            assertThat(success.pendingBooks).isEmpty()
            assertThat(success.readingBooks).isEmpty()
        }
    }

    @Test
    fun `onEvent OnDeleteBookClick snapshots daily readings and calls deleteBookUseCase`() = runTest {
        val book = Book(id = 1, title = "T", author = "A", isPhysical = false)
        val dailyReading = DailyReading(
            bookId = 1,
            date = "2024-01-01",
            timeSpentMillis = 100,
            reachedPercentage = 0.5f,
            readingSpeed = 200
        )
        every { getBookDailyReadingsUseCase(1) } returns flowOf(listOf(dailyReading))
        coEvery { getReadingProgressByIdUseCase(1) } returns null
        coEvery { deleteBookUseCase(1) } returns Result.success(Unit)

        viewModel.onEvent(HomeEvent.OnDeleteBookClick(book))

        coVerify { getBookDailyReadingsUseCase(1) }
        coVerify { getReadingProgressByIdUseCase(1) }
        coVerify { deleteBookUseCase(1) }
    }

    @Test
    fun `onEvent OnUndoDeleteClick calls restoreDeletedBookUseCase with snapshot`() = runTest {
        val book = Book(id = 1, title = "T", author = "A", isPhysical = false)
        val dailyReading = DailyReading(
            bookId = 1,
            date = "2024-01-01",
            timeSpentMillis = 100,
            reachedPercentage = 0.5f,
            readingSpeed = 200
        )
        val readingProgress = ReadingProgress(
            bookId = 1,
            totalChapters = 20,
            lastChapterIndex = 5,
            scrollPosition = 0.75f,
            lastReadAt = 100L
        )
        val expectedSnapshot = DeletedBookSnapshot(
            book = book,
            dailyReadings = listOf(dailyReading),
            readingProgress = readingProgress
        )

        every { getBookDailyReadingsUseCase(1) } returns flowOf(listOf(dailyReading))
        coEvery { getReadingProgressByIdUseCase(1) } returns readingProgress
        coEvery { deleteBookUseCase(1) } returns Result.success(Unit)
        coEvery { restoreDeletedBookUseCase(any()) } returns Result.success(Unit)

        viewModel.onEvent(HomeEvent.OnDeleteBookClick(book))
        assertThat(viewModel.pendingUndoBookId.value).isEqualTo(1)
        viewModel.onEvent(HomeEvent.OnUndoDeleteClick(1))

        coVerify { restoreDeletedBookUseCase(expectedSnapshot) }
        assertThat(viewModel.pendingUndoBookId.value).isNull()
    }

    @Test
    fun `onEvent OnUndoDeleteClick calls restoreDeletedBookUseCase with null progress when absent`() = runTest {
        val book = Book(id = 1, title = "T", author = "A", isPhysical = false)
        val expectedSnapshot = DeletedBookSnapshot(
            book = book,
            dailyReadings = emptyList(),
            readingProgress = null
        )

        every { getBookDailyReadingsUseCase(1) } returns flowOf(emptyList())
        coEvery { getReadingProgressByIdUseCase(1) } returns null
        coEvery { deleteBookUseCase(1) } returns Result.success(Unit)
        coEvery { restoreDeletedBookUseCase(any()) } returns Result.success(Unit)

        viewModel.onEvent(HomeEvent.OnDeleteBookClick(book))
        viewModel.onEvent(HomeEvent.OnUndoDeleteClick(1))

        coVerify { restoreDeletedBookUseCase(expectedSnapshot) }
    }

    @Test
    fun `onEvent OnUndoDeleteClick emits restored snackbar on success`() = runTest {
        val book = Book(id = 1, title = "T", author = "A", isPhysical = false)

        every { getBookDailyReadingsUseCase(1) } returns flowOf(emptyList())
        coEvery { getReadingProgressByIdUseCase(1) } returns null
        coEvery { deleteBookUseCase(1) } returns Result.success(Unit)
        coEvery { restoreDeletedBookUseCase(any()) } returns Result.success(Unit)

        viewModel.effect.test {
            viewModel.onEvent(HomeEvent.OnDeleteBookClick(book))
            assertThat(viewModel.pendingUndoBookId.value).isEqualTo(1)

            viewModel.onEvent(HomeEvent.OnUndoDeleteClick(1))

            val restoreEffect = awaitItem() as UiEffect.ShowSnackbar
            assertThat((restoreEffect.message as UiText.StringResource).resId).isEqualTo(R.string.book_restored)
        }
    }

    @Test
    fun `onEvent OnDeleteConfirmed calls FileUtils delete`() = runTest {
        every { FileUtils.deleteFileFromInternalStorage(any(), any()) } returns Unit

        val book = Book(id = 1, title = "T", author = "A", isPhysical = false, coverUrl = "c", localFilePath = "l")
        every { getBookDailyReadingsUseCase(1) } returns flowOf(emptyList())
        coEvery { getReadingProgressByIdUseCase(1) } returns null
        coEvery { deleteBookUseCase(1) } returns Result.success(Unit)

        viewModel.onEvent(HomeEvent.OnDeleteBookClick(book))
        viewModel.onEvent(HomeEvent.OnDeleteConfirmed(1))

        coVerify { FileUtils.deleteFileFromInternalStorage(any(), "c") }
        coVerify { FileUtils.deleteFileFromInternalStorage(any(), "l") }
        assertThat(viewModel.pendingUndoBookId.value).isNull()
    }

    @Test
    fun `second delete confirms first pending files and keeps undo for second`() = runTest {
        every { FileUtils.deleteFileFromInternalStorage(any(), any()) } returns Unit

        val bookA = Book(id = 1, title = "A", author = "A", isPhysical = false, coverUrl = "cA", localFilePath = "lA")
        val bookB = Book(id = 2, title = "B", author = "B", isPhysical = false, coverUrl = "cB", localFilePath = "lB")
        every { getBookDailyReadingsUseCase(any()) } returns flowOf(emptyList())
        coEvery { getReadingProgressByIdUseCase(any()) } returns null
        coEvery { deleteBookUseCase(any()) } returns Result.success(Unit)
        coEvery { restoreDeletedBookUseCase(any()) } returns Result.success(Unit)

        viewModel.onEvent(HomeEvent.OnDeleteBookClick(bookA))
        assertThat(viewModel.pendingUndoBookId.value).isEqualTo(1)

        viewModel.onEvent(HomeEvent.OnDeleteBookClick(bookB))
        assertThat(viewModel.pendingUndoBookId.value).isEqualTo(2)

        coVerify { FileUtils.deleteFileFromInternalStorage(any(), "cA") }
        coVerify { FileUtils.deleteFileFromInternalStorage(any(), "lA") }
        coVerify(exactly = 0) { FileUtils.deleteFileFromInternalStorage(any(), "cB") }
        coVerify(exactly = 0) { FileUtils.deleteFileFromInternalStorage(any(), "lB") }

        viewModel.onEvent(HomeEvent.OnDeleteConfirmed(1))
        coVerify(exactly = 0) { FileUtils.deleteFileFromInternalStorage(any(), "cB") }

        viewModel.onEvent(HomeEvent.OnUndoDeleteClick(2))
        coVerify {
            restoreDeletedBookUseCase(
                DeletedBookSnapshot(book = bookB, dailyReadings = emptyList(), readingProgress = null)
            )
        }
        assertThat(viewModel.pendingUndoBookId.value).isNull()
    }

    @Test
    fun `onEvent OnImportBook calls use case`() = runTest {
        val uri = mockk<Uri>()
        coEvery { importBookFromUriUseCase(any(), any()) } returns Result.success(Unit)

        viewModel.onEvent(HomeEvent.OnImportBook(uri))

        coVerify { importBookFromUriUseCase(any(), any()) }
    }
}
