package com.juguito.juguitoreader.ui.book.add

import android.app.Application
import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.usecase.book.AddBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBookFromEpubUseCase
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
class AddBookViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: AddBookViewModel
    private val application = mockk<Application>(relaxed = true)
    private val addBookUseCase = mockk<AddBookUseCase>()
    private val getFoldersUseCase = mockk<GetFoldersUseCase>()
    private val getGenresUseCase = mockk<GetGenresUseCase>()
    private val getBookFromEpubUseCase = mockk<GetBookFromEpubUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Uri::class)
        mockkStatic("androidx.core.net.UriKt")
        val parsedUri = mockk<Uri>(relaxed = true)
        every { Uri.parse(any()) } returns parsedUri

        mockkObject(FileUtils)
        every { FileUtils.deleteStagingAsset(any(), any()) } just runs
        every { FileUtils.deleteFileFromInternalStorage(any(), any()) } just runs

        every { getFoldersUseCase() } returns flowOf(emptyList())
        every { getGenresUseCase() } returns flowOf(emptyList())

        viewModel = AddBookViewModel(
            application,
            addBookUseCase,
            getFoldersUseCase,
            getGenresUseCase,
            getBookFromEpubUseCase
        )
    }

    @After
    fun teardown() = runBlocking {
        drainIoDispatcher()
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `onEvent updates draft correctly for all fields`() = runTest {
        viewModel.onEvent(AddBookEvent.OnTitleChanged("Title"))
        viewModel.onEvent(AddBookEvent.OnAuthorChanged("Author"))
        viewModel.onEvent(AddBookEvent.OnPublisherChanged("Pub"))
        viewModel.onEvent(AddBookEvent.OnSeriesChanged("Series"))
        viewModel.onEvent(AddBookEvent.OnSeriesOrderChanged("1"))
        viewModel.onEvent(AddBookEvent.OnIsPhysicalChanged(true))

        val folders = listOf(Folder(name = "F", colorHex = "#000"))
        viewModel.onEvent(AddBookEvent.OnFoldersChanged(folders))

        val genres = listOf(Genre(name = "G"))
        viewModel.onEvent(AddBookEvent.OnGenresChanged(genres))

        val state = viewModel.uiState.value
        assertThat(state.bookDraft.title).isEqualTo("Title")
        assertThat(state.bookDraft.author).isEqualTo("Author")
        assertThat(state.bookDraft.publisher).isEqualTo("Pub")
        assertThat(state.bookDraft.series).isEqualTo("Series")
        assertThat(state.bookDraft.seriesOrder).isEqualTo("1")
        assertThat(state.bookDraft.isPhysical).isTrue()
        assertThat(state.bookDraft.folders).isEqualTo(folders)
        assertThat(state.bookDraft.genres).isEqualTo(genres)
    }

    @Test
    fun `onEvent OnCoverChanged stores path in draft and deletes previous staging cover`() = runTest {
        viewModel.onEvent(AddBookEvent.OnCoverChanged("/cache/images/old.jpg"))
        viewModel.onEvent(AddBookEvent.OnCoverChanged("content://picker/cover.jpg"))

        assertThat(viewModel.uiState.value.bookDraft.coverUrl).isEqualTo("content://picker/cover.jpg")
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteStagingAsset(application, "/cache/images/old.jpg")
        }
        verify(exactly = 0) { FileUtils.saveImageToInternalStorage(any(), any()) }
    }

    @Test
    fun `onEvent OnEpubFilePicked stores uri in draft without copying`() = runTest {
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://picker/book.epub"

        viewModel.onEvent(AddBookEvent.OnEpubFilePicked(uri))

        assertThat(viewModel.uiState.value.bookDraft.localFilePath).isEqualTo("content://picker/book.epub")
        verify(exactly = 0) { FileUtils.saveEpubBookToInternalStorage(any(), any()) }
    }

    @Test
    fun `onEvent OnLocalFilePathChanged null clears draft path`() = runTest {
        viewModel.onEvent(AddBookEvent.OnLocalFilePathChanged("local/path"))
        viewModel.onEvent(AddBookEvent.OnLocalFilePathChanged(null))
        assertThat(viewModel.uiState.value.bookDraft.localFilePath).isNull()
    }

    @Test
    fun `onEvent OnSaveClick promotes files before addBook`() = runTest {
        val epubUri = mockk<Uri>(relaxed = true)
        every { epubUri.toString() } returns "content://picker/book.epub"
        viewModel.onEvent(AddBookEvent.OnTitleChanged("T"))
        viewModel.onEvent(AddBookEvent.OnAuthorChanged("A"))
        viewModel.onEvent(AddBookEvent.OnEpubFilePicked(epubUri))
        every {
            FileUtils.promotePendingFiles(application, "content://picker/book.epub", null)
        } returns PromotedBookFiles(epubPath = "/data/files/book.epub", coverPath = null)
        coEvery { addBookUseCase(any()) } returns Result.success(Unit)

        viewModel.onEvent(AddBookEvent.OnSaveClick)

        coVerify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            addBookUseCase(match { it.localFilePath == "/data/files/book.epub" })
        }
        viewModel.effect.test {
            assertThat(awaitItem()).isEqualTo(UiEffect.NavigateBack)
        }
    }

    @Test
    fun `onEvent OnSaveClick physical does not promote leftover epub`() = runTest {
        val epubUri = mockk<Uri>(relaxed = true)
        every { epubUri.toString() } returns "content://picker/book.epub"
        viewModel.onEvent(AddBookEvent.OnTitleChanged("T"))
        viewModel.onEvent(AddBookEvent.OnAuthorChanged("A"))
        viewModel.onEvent(AddBookEvent.OnEpubFilePicked(epubUri))
        viewModel.onEvent(AddBookEvent.OnIsPhysicalChanged(true))
        every {
            FileUtils.promotePendingFiles(application, null, null)
        } returns PromotedBookFiles(epubPath = null, coverPath = null)
        coEvery { addBookUseCase(any()) } returns Result.success(Unit)

        viewModel.onEvent(AddBookEvent.OnSaveClick)

        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.promotePendingFiles(application, null, null)
        }
        coVerify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            addBookUseCase(match { it.isPhysical && it.localFilePath == null })
        }
    }

    @Test
    fun `onEvent OnSaveClick shows snackbar and rolls back when addBook fails`() = runTest {
        viewModel.onEvent(AddBookEvent.OnTitleChanged("T"))
        viewModel.onEvent(AddBookEvent.OnAuthorChanged("A"))
        every {
            FileUtils.promotePendingFiles(any(), any(), any())
        } returns PromotedBookFiles(
            epubPath = "/data/files/book.epub",
            coverPath = "/data/files/cover.jpg",
        )
        coEvery { addBookUseCase(any()) } returns Result.failure(
            JuguitoException(R.string.error_save_book)
        )

        viewModel.onEvent(AddBookEvent.OnSaveClick)

        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            assertEquals(R.string.error_save_book, (effect.message as UiText.StringResource).resId)
        }
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteFileFromInternalStorage(application, "/data/files/book.epub")
        }
        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteFileFromInternalStorage(application, "/data/files/cover.jpg")
        }
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `onEvent OnSaveClick shows snackbar when promote fails`() = runTest {
        viewModel.onEvent(AddBookEvent.OnTitleChanged("T"))
        viewModel.onEvent(AddBookEvent.OnAuthorChanged("A"))
        every {
            FileUtils.promotePendingFiles(any(), any(), any())
        } throws JuguitoException(R.string.error_copy_epub)

        viewModel.onEvent(AddBookEvent.OnSaveClick)

        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            assertEquals(R.string.error_copy_epub, (effect.message as UiText.StringResource).resId)
        }
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        coVerify(exactly = 0) { addBookUseCase(any()) }
    }

    @Test
    fun `onEvent OnSaveClick validates blank fields`() = runTest {
        viewModel.onEvent(AddBookEvent.OnSaveClick)
        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            val uiText = effect.message as UiText.StringResource
            assertEquals(R.string.error_title_empty, uiText.resId)
        }
    }

    @Test
    fun `onEvent OnDiscard deletes staging cover`() = runTest {
        viewModel.onEvent(AddBookEvent.OnCoverChanged("/cache/images/cover.jpg"))

        viewModel.onEvent(AddBookEvent.OnDiscard)

        verify(timeout = IO_DISPATCHER_TIMEOUT_MS) {
            FileUtils.deleteStagingAsset(application, "/cache/images/cover.jpg")
        }
    }

    @Test
    fun `onEvent OnImportEpub loads metadata without persisting files`() = runTest {
        val uri = mockk<Uri>(relaxed = true)
        val book = Book(
            title = "Epub Title",
            author = "Epub Author",
            isPhysical = false,
            localFilePath = "content://picker/imported.epub",
            coverUrl = "/cache/covers/imported.jpg",
        )
        coEvery { getBookFromEpubUseCase(application, uri, false) } returns book

        viewModel.onEvent(AddBookEvent.OnImportEpub(uri))
        viewModel.uiState.awaitValue { it.bookDraft.title == "Epub Title" && !it.isLoading }

        assertThat(viewModel.uiState.value.bookDraft.localFilePath)
            .isEqualTo("content://picker/imported.epub")
        assertThat(viewModel.uiState.value.bookDraft.coverUrl)
            .isEqualTo("/cache/covers/imported.jpg")
        coVerify { getBookFromEpubUseCase(application, uri, persistFiles = false) }
    }

    @Test
    fun `onEvent OnImportEpub clears loading state on failure`() = runTest {
        val uri = mockk<Uri>(relaxed = true)
        coEvery { getBookFromEpubUseCase(application, uri, false) } throws
            JuguitoException(R.string.error_copy_epub)

        viewModel.onEvent(AddBookEvent.OnImportEpub(uri))
        viewModel.uiState.awaitValue { !it.isLoading }

        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `onEvent OnSeriesOrderChanged allows decimals and rejects invalid input`() = runTest {
        viewModel.onEvent(AddBookEvent.OnSeriesOrderChanged("1.5"))
        assertThat(viewModel.uiState.value.bookDraft.seriesOrder).isEqualTo("1.5")

        viewModel.onEvent(AddBookEvent.OnSeriesOrderChanged("1,5"))
        assertThat(viewModel.uiState.value.bookDraft.seriesOrder).isEqualTo("1.5")

        viewModel.onEvent(AddBookEvent.OnSeriesOrderChanged("12.34"))
        assertThat(viewModel.uiState.value.bookDraft.seriesOrder).isEqualTo("12.34")

        viewModel.onEvent(AddBookEvent.OnSeriesOrderChanged("1."))
        assertThat(viewModel.uiState.value.bookDraft.seriesOrder).isEqualTo("1.")

        viewModel.onEvent(AddBookEvent.OnSeriesOrderChanged("12.345"))
        assertThat(viewModel.uiState.value.bookDraft.seriesOrder).isEqualTo("1.")

        viewModel.onEvent(AddBookEvent.OnSeriesOrderChanged("abc"))
        assertThat(viewModel.uiState.value.bookDraft.seriesOrder).isEqualTo("1.")

        viewModel.onEvent(AddBookEvent.OnSeriesOrderChanged("100"))
        assertThat(viewModel.uiState.value.bookDraft.seriesOrder).isEqualTo("1.")
    }
}
