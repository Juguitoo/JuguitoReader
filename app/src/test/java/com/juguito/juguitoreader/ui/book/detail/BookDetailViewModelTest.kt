package com.juguito.juguitoreader.ui.book.detail

import android.app.Application
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.usecase.book.DeleteBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBookByIdUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import com.juguito.juguitoreader.domain.usecase.genre.GetGenresUseCase
import com.juguito.juguitoreader.testutil.IO_DISPATCHER_TIMEOUT_MS
import com.juguito.juguitoreader.testutil.awaitValue
import com.juguito.juguitoreader.testutil.drainIoDispatcher
import com.juguito.juguitoreader.ui.common.UiText
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.utils.FileUtils
import com.juguito.juguitoreader.utils.PromotedBookFiles
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: BookDetailViewModel
    private val application = mockk<Application>(relaxed = true)
    private val getBookByIdUseCase = mockk<GetBookByIdUseCase>()
    private val updateBookUseCase = mockk<UpdateBookUseCase>()
    private val getFoldersUseCase = mockk<GetFoldersUseCase>()
    private val getGenresUseCase = mockk<GetGenresUseCase>()
    private val deleteBookUseCase = mockk<DeleteBookUseCase>()
    private val savedStateHandle = SavedStateHandle(mapOf("bookId" to 1))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Uri::class)
        mockkStatic("androidx.core.net.UriKt")

        mockkObject(FileUtils)
        every { FileUtils.deleteStagingAsset(any(), any()) } just runs
        every { FileUtils.deleteFileFromInternalStorage(any(), any()) } just runs
        every { FileUtils.deleteReaderCache(any(), any()) } returns true
        every {
            FileUtils.promotePendingFiles(any(), any(), any())
        } returns PromotedBookFiles(epubPath = null, coverPath = null)

        every { getFoldersUseCase() } returns flowOf(emptyList())
        every { getGenresUseCase() } returns flowOf(emptyList())
        coEvery { getBookByIdUseCase(1) } returns Book(
            id = 1,
            title = "Title",
            author = "Author",
            isPhysical = true,
        )

        viewModel = BookDetailViewModel(
            application,
            getBookByIdUseCase,
            updateBookUseCase,
            getFoldersUseCase,
            getGenresUseCase,
            deleteBookUseCase,
            savedStateHandle,
        )
    }

    private suspend fun loadDigitalBook(
        epubPath: String = "/data/files/persisted.epub",
        coverPath: String? = "/data/files/persisted.jpg",
    ) {
        coEvery { getBookByIdUseCase(1) } returns Book(
            id = 1,
            title = "Title",
            author = "Author",
            isPhysical = false,
            localFilePath = epubPath,
            coverUrl = coverPath,
        )
        viewModel.loadBook()
        viewModel.uiState.awaitValue {
            it is BookDetailUiState.Success && it.bookDraft.localFilePath == epubPath
        }
    }

    @After
    fun teardown() = runBlocking {
        drainIoDispatcher()
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial loadBook sets Success state`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(BookDetailUiState.Success::class.java)
            val success = state as BookDetailUiState.Success
            assertThat(success.book.title).isEqualTo("Title")
        }
    }

    @Test
    fun `onEvent updates draft correctly for all simple fields`() = runTest {
        viewModel.onEvent(BookDetailEvent.OnTitleChanged("New Title"))
        viewModel.onEvent(BookDetailEvent.OnAuthorChanged("New Author"))
        viewModel.onEvent(BookDetailEvent.OnPublisherChanged("New Publisher"))
        viewModel.onEvent(BookDetailEvent.OnSeriesChanged("New Series"))
        viewModel.onEvent(BookDetailEvent.OnSeriesOrderChanged("10"))
        viewModel.onEvent(BookDetailEvent.OnIsPhysicalChanged(false))
        viewModel.onEvent(BookDetailEvent.OnRatingChanged(4.5f))
        viewModel.onEvent(BookDetailEvent.OnCommentChanged("Nice book"))
        viewModel.onEvent(BookDetailEvent.OnStatusChanged(BookStatus.READING))
        viewModel.onEvent(BookDetailEvent.OnTabChanged(1))

        viewModel.uiState.test {
            val success = awaitItem() as BookDetailUiState.Success
            assertThat(success.bookDraft.title).isEqualTo("New Title")
            assertThat(success.bookDraft.author).isEqualTo("New Author")
            assertThat(success.bookDraft.publisher).isEqualTo("New Publisher")
            assertThat(success.bookDraft.series).isEqualTo("New Series")
            assertThat(success.bookDraft.seriesOrder).isEqualTo("10")
            assertThat(success.bookDraft.isPhysical).isFalse()
            assertThat(success.rating).isEqualTo(4.5f)
            assertThat(success.comment).isEqualTo("Nice book")
            assertThat(success.status).isEqualTo(BookStatus.READING)
            assertThat(success.selectedTab).isEqualTo(1)
        }
    }

    @Test
    fun `onEvent OnFoldersChanged and OnGenresChanged updates draft`() = runTest {
        val folders = listOf(Folder(id = 1, name = "F", colorHex = "#000"))
        val genres = listOf(Genre(id = 1, name = "G"))

        viewModel.onEvent(BookDetailEvent.OnFoldersChanged(folders))
        viewModel.onEvent(BookDetailEvent.OnGenresChanged(genres))

        viewModel.uiState.test {
            val success = awaitItem() as BookDetailUiState.Success
            assertThat(success.bookDraft.folders).isEqualTo(folders)
            assertThat(success.bookDraft.genres).isEqualTo(genres)
        }
    }

    @Test
    fun `onEvent OnStartDateChanged and OnEndDateChanged updates state`() = runTest {
        viewModel.onEvent(BookDetailEvent.OnStartDateChanged(1000L))
        viewModel.onEvent(BookDetailEvent.OnEndDateChanged(2000L))

        viewModel.uiState.test {
            val success = awaitItem() as BookDetailUiState.Success
            assertThat(success.startDate).isEqualTo(1000L)
            assertThat(success.endDate).isEqualTo(2000L)
            assertThat(success.status).isEqualTo(BookStatus.FINISHED)
        }
    }

    @Test
    fun `onEvent OnEditModeChanged toggles edit mode and resets draft if false`() = runTest {
        viewModel.onEvent(BookDetailEvent.OnTitleChanged("Modified"))
        viewModel.onEvent(BookDetailEvent.OnEditModeChanged(false))

        viewModel.uiState.test {
            val success = awaitItem() as BookDetailUiState.Success
            assertThat(success.isEditMode).isFalse()
            assertThat(success.bookDraft.title).isEqualTo("Title")
        }
    }

    @Test
    fun `onEvent OnEditModeChanged false deletes staging cover from draft`() = runTest {
        viewModel.onEvent(BookDetailEvent.OnCoverChanged("/cache/covers/staging.jpg"))
        viewModel.onEvent(BookDetailEvent.OnEditModeChanged(false))

        verify { FileUtils.deleteStagingAsset(application, "/cache/covers/staging.jpg") }
    }

    @Test
    fun `onEvent OnDiscard deletes staging cover and navigates back`() = runTest {
        viewModel.onEvent(BookDetailEvent.OnCoverChanged("/cache/covers/staging.jpg"))
        viewModel.onEvent(BookDetailEvent.OnDiscard)

        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteStagingAsset(application, "/cache/covers/staging.jpg")
        }
        viewModel.effect.test {
            assertThat(awaitItem()).isEqualTo(UiEffect.NavigateBack)
        }
    }

    @Test
    fun `onEvent OnCoverChanged stores path in draft and deletes previous staging cover`() = runTest {
        viewModel.onEvent(BookDetailEvent.OnCoverChanged("/cache/images/old.jpg"))
        viewModel.onEvent(BookDetailEvent.OnCoverChanged("content://picker/cover.jpg"))

        val success = viewModel.uiState.value as BookDetailUiState.Success
        assertThat(success.bookDraft.coverUrl).isEqualTo("content://picker/cover.jpg")
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteStagingAsset(application, "/cache/images/old.jpg")
        }
        verify(exactly = 0) { FileUtils.saveImageToInternalStorage(any(), any()) }
    }

    @Test
    fun `onEvent OnEpubFilePicked stores uri in draft without copying`() = runTest {
        loadDigitalBook()
        val pickerUri = mockk<Uri>(relaxed = true)
        every { pickerUri.toString() } returns "content://documents/book.epub"

        viewModel.onEvent(BookDetailEvent.OnEpubFilePicked(pickerUri))

        val success = viewModel.uiState.value as BookDetailUiState.Success
        assertThat(success.bookDraft.localFilePath).isEqualTo("content://documents/book.epub")
        verify(exactly = 0) { FileUtils.saveEpubBookToInternalStorage(any(), any()) }
    }

    @Test
    fun `onEvent OnLocalFilePathChanged null clears draft without deleting persisted file`() = runTest {
        loadDigitalBook()

        viewModel.onEvent(BookDetailEvent.OnLocalFilePathChanged(null))

        val success = viewModel.uiState.value as BookDetailUiState.Success
        assertThat(success.bookDraft.localFilePath).isNull()
        verify(exactly = 0) { FileUtils.deleteFileFromInternalStorage(any(), "/data/files/persisted.epub") }
    }

    @Test
    fun `FILE-006 OnSaveClick does not pass unchanged persisted paths to promote`() = runTest {
        loadDigitalBook()
        coEvery { updateBookUseCase(any()) } returns Result.success(Unit)

        viewModel.onEvent(BookDetailEvent.OnSaveClick)

        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.promotePendingFiles(application, null, null)
        }
        coVerify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            updateBookUseCase(match {
                it.localFilePath == "/data/files/persisted.epub" &&
                    it.coverUrl == "/data/files/persisted.jpg"
            })
        }
        verify(exactly = 0) {
            FileUtils.deleteFileFromInternalStorage(any(), "/data/files/persisted.epub")
        }
        verify(exactly = 0) {
            FileUtils.deleteFileFromInternalStorage(any(), "/data/files/persisted.jpg")
        }
    }

    @Test
    fun `FILE-006 OnSaveClick promotes only new cover and keeps persisted epub`() = runTest {
        loadDigitalBook()
        viewModel.onEvent(BookDetailEvent.OnCoverChanged("content://picker/cover.jpg"))
        every {
            FileUtils.promotePendingFiles(application, null, "content://picker/cover.jpg")
        } returns PromotedBookFiles(
            epubPath = null,
            coverPath = "/data/files/new.jpg",
        )
        coEvery { updateBookUseCase(any()) } returns Result.success(Unit)

        viewModel.onEvent(BookDetailEvent.OnSaveClick)

        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.promotePendingFiles(application, null, "content://picker/cover.jpg")
        }
        coVerify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            updateBookUseCase(match {
                it.localFilePath == "/data/files/persisted.epub" &&
                    it.coverUrl == "/data/files/new.jpg"
            })
        }
        verify(exactly = 0) {
            FileUtils.deleteFileFromInternalStorage(any(), "/data/files/persisted.epub")
        }
    }

    @Test
    fun `FILE-006 OnSaveClick does not delete persisted epub when new cover promote fails`() = runTest {
        loadDigitalBook()
        viewModel.onEvent(BookDetailEvent.OnCoverChanged("content://picker/cover.jpg"))
        every {
            FileUtils.promotePendingFiles(application, null, "content://picker/cover.jpg")
        } throws JuguitoException(R.string.error_copy_cover)

        viewModel.onEvent(BookDetailEvent.OnSaveClick)

        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            assertEquals(R.string.error_copy_cover, (effect.message as UiText.StringResource).resId)
        }
        coVerify(exactly = 0) { updateBookUseCase(any()) }
        verify(exactly = 0) { FileUtils.deleteFileFromInternalStorage(any(), any()) }
        val success = viewModel.uiState.value as BookDetailUiState.Success
        assertThat(success.book.localFilePath).isEqualTo("/data/files/persisted.epub")
        assertThat(success.book.coverUrl).isEqualTo("/data/files/persisted.jpg")
    }

    @Test
    fun `onEvent OnSaveClick promotes files before updateBook`() = runTest {
        loadDigitalBook()
        every {
            FileUtils.promotePendingFiles(any(), any(), any())
        } returns PromotedBookFiles(
            epubPath = "/data/files/persisted.epub",
            coverPath = "/data/files/persisted.jpg",
        )
        coEvery { updateBookUseCase(any()) } returns Result.success(Unit)

        viewModel.onEvent(BookDetailEvent.OnSaveClick)

        coVerify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            updateBookUseCase(match {
                it.localFilePath == "/data/files/persisted.epub" &&
                    it.coverUrl == "/data/files/persisted.jpg"
            })
        }
        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            assertEquals(R.string.save_success, (effect.message as UiText.StringResource).resId)
        }
    }

    @Test
    fun `onEvent OnSaveClick deletes previous persisted epub when path changed`() = runTest {
        loadDigitalBook()
        val pickerUri = mockk<Uri>(relaxed = true)
        every { pickerUri.toString() } returns "content://documents/new.epub"
        viewModel.onEvent(BookDetailEvent.OnEpubFilePicked(pickerUri))
        every {
            FileUtils.promotePendingFiles(any(), any(), any())
        } returns PromotedBookFiles(
            epubPath = "/data/files/new.epub",
            coverPath = "/data/files/persisted.jpg",
        )
        coEvery { updateBookUseCase(any()) } returns Result.success(Unit)

        viewModel.onEvent(BookDetailEvent.OnSaveClick)

        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            assertEquals(R.string.save_success, (effect.message as UiText.StringResource).resId)
        }
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteFileFromInternalStorage(any(), "/data/files/persisted.epub")
        }
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteReaderCache(any(), 1)
        }
        assertThat((viewModel.uiState.value as BookDetailUiState.Success).isActionLoading).isFalse()
    }

    @Test
    fun `onEvent OnSaveClick rolls back only promoted files when update fails`() = runTest {
        loadDigitalBook()
        val pickerUri = mockk<Uri>(relaxed = true)
        every { pickerUri.toString() } returns "content://documents/new.epub"
        viewModel.onEvent(BookDetailEvent.OnEpubFilePicked(pickerUri))
        every {
            FileUtils.promotePendingFiles(any(), any(), any())
        } returns PromotedBookFiles(
            epubPath = "/data/files/new.epub",
            coverPath = "/data/files/persisted.jpg",
        )
        coEvery { updateBookUseCase(any()) } returns Result.failure(
            JuguitoException(R.string.error_save_book),
        )

        viewModel.onEvent(BookDetailEvent.OnSaveClick)

        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            assertEquals(R.string.error_save_book, (effect.message as UiText.StringResource).resId)
        }
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteFileFromInternalStorage(any(), "/data/files/new.epub")
        }
        verify(exactly = 0) {
            FileUtils.deleteFileFromInternalStorage(any(), "/data/files/persisted.epub")
        }
        verify(exactly = 0) { FileUtils.deleteReaderCache(any(), any()) }
        assertThat((viewModel.uiState.value as BookDetailUiState.Success).isActionLoading).isFalse()
    }

    @Test
    fun `onEvent OnSaveClick keeps persisted files when update fails without path change`() = runTest {
        loadDigitalBook()
        every {
            FileUtils.promotePendingFiles(any(), any(), any())
        } returns PromotedBookFiles(
            epubPath = "/data/files/persisted.epub",
            coverPath = "/data/files/persisted.jpg",
        )
        coEvery { updateBookUseCase(any()) } returns Result.failure(
            JuguitoException(R.string.error_save_book),
        )

        viewModel.onEvent(BookDetailEvent.OnSaveClick)

        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            assertEquals(R.string.error_save_book, (effect.message as UiText.StringResource).resId)
        }
        verify(exactly = 0) { FileUtils.deleteFileFromInternalStorage(any(), any()) }
        verify(exactly = 0) { FileUtils.deleteReaderCache(any(), any()) }
        assertThat((viewModel.uiState.value as BookDetailUiState.Success).isActionLoading).isFalse()
    }

    @Test
    fun `onEvent OnSaveClick shows snackbar when promote fails`() = runTest {
        loadDigitalBook()
        every {
            FileUtils.promotePendingFiles(any(), any(), any())
        } throws JuguitoException(R.string.error_copy_epub)

        viewModel.onEvent(BookDetailEvent.OnSaveClick)

        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            assertEquals(R.string.error_copy_epub, (effect.message as UiText.StringResource).resId)
        }
        coVerify(exactly = 0) { updateBookUseCase(any()) }
        assertThat((viewModel.uiState.value as BookDetailUiState.Success).isActionLoading).isFalse()
    }

    @Test
    fun `onEvent OnDeleteClick deletes book files and reader cache`() = runTest {
        loadDigitalBook()
        coEvery { deleteBookUseCase(1) } returns Result.success(Unit)

        viewModel.onEvent(BookDetailEvent.OnDeleteClick)

        coVerify(timeout = IO_DISPATCHER_TIMEOUT_MS) { deleteBookUseCase(1) }
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteFileFromInternalStorage(any(), "/data/files/persisted.epub")
        }
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteFileFromInternalStorage(any(), "/data/files/persisted.jpg")
        }
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteReaderCache(any(), 1)
        }
        viewModel.effect.test {
            assertThat(awaitItem()).isEqualTo(UiEffect.NavigateBack)
        }
    }

    @Test
    fun `onEvent OnDeleteClick shows snackbar when delete throws`() = runTest {
        loadDigitalBook()
        coEvery { deleteBookUseCase(1) } returns Result.failure(JuguitoException(R.string.error_delete_book))

        viewModel.onEvent(BookDetailEvent.OnDeleteClick)

        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            assertEquals(R.string.error_delete_book, (effect.message as UiText.StringResource).resId)
        }
        verify(exactly = 0) { FileUtils.deleteFileFromInternalStorage(any(), any()) }
        assertThat((viewModel.uiState.value as BookDetailUiState.Success).isActionLoading).isFalse()
    }

    @Test
    fun `loadBook handles non-existing book`() = runTest {
        coEvery { getBookByIdUseCase(1) } returns null
        viewModel.loadBook()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(BookDetailUiState.Error::class.java)
        }
    }

    @Test
    fun `FILE-017 OnSaveClick physical drops persisted epub and deletes file`() = runTest {
        loadDigitalBook()
        viewModel.onEvent(BookDetailEvent.OnIsPhysicalChanged(true))
        coEvery { updateBookUseCase(any()) } returns Result.success(Unit)

        viewModel.onEvent(BookDetailEvent.OnSaveClick)

        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.promotePendingFiles(application, null, null)
        }
        coVerify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            updateBookUseCase(match { it.isPhysical && it.localFilePath == null })
        }
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteFileFromInternalStorage(any(), "/data/files/persisted.epub")
        }
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteReaderCache(any(), 1)
        }
        verify(exactly = 0) {
            FileUtils.deleteFileFromInternalStorage(any(), "/data/files/persisted.jpg")
        }
    }

    @Test
    fun `FILE-017 OnSaveClick digital with cleared path persists null and deletes file`() = runTest {
        loadDigitalBook()
        viewModel.onEvent(BookDetailEvent.OnLocalFilePathChanged(null))
        coEvery { updateBookUseCase(any()) } returns Result.success(Unit)

        viewModel.onEvent(BookDetailEvent.OnSaveClick)

        coVerify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            updateBookUseCase(match { !it.isPhysical && it.localFilePath == null })
        }
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteFileFromInternalStorage(any(), "/data/files/persisted.epub")
        }
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteReaderCache(any(), 1)
        }
    }

    @Test
    fun `FILE-015 OnSaveClick twice calls updateBook once`() = runTest {
        val latch = CompletableDeferred<Result<Unit>>()
        coEvery { updateBookUseCase(any()) } coAnswers { latch.await() }

        try {
            viewModel.onEvent(BookDetailEvent.OnSaveClick)
            viewModel.onEvent(BookDetailEvent.OnSaveClick)

            coVerify(timeout = IO_DISPATCHER_TIMEOUT_MS, exactly = 1) { updateBookUseCase(any()) }
        } finally {
            latch.complete(Result.success(Unit))
        }
    }
}
