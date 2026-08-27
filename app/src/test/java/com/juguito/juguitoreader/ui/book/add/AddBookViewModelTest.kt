package com.juguito.juguitoreader.ui.book.add

import android.app.Application
import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.usecase.book.AddBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBookFromEpubUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import com.juguito.juguitoreader.domain.usecase.genre.GetGenresUseCase
import com.juguito.juguitoreader.ui.common.UiText
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.utils.FileUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
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
        val uri = mockk<Uri>(relaxed = true)
        every { Uri.parse(any()) } returns uri
        
        mockkObject(FileUtils)
        
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
    fun teardown() {
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
    fun `onEvent OnCoverUrlChanged calls FileUtils and updates draft`() = runTest {
        every { FileUtils.saveImageToInternalStorage(any(), any()) } returns "new/path"
        
        viewModel.onEvent(AddBookEvent.OnCoverUrlChanged("temp/uri"))
        
        // As it launches a coroutine, we use turbine to wait for the state update
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.bookDraft.coverUrl).isEqualTo("new/path")
        }
    }

    @Test
    fun `onEvent OnLocalFilePathChanged updates draft`() = runTest {
        viewModel.onEvent(AddBookEvent.OnLocalFilePathChanged("local/path"))
        assertThat(viewModel.uiState.value.bookDraft.localFilePath).isEqualTo("local/path")
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
    fun `onEvent OnSaveClick calls use case on success`() = runTest {
        viewModel.onEvent(AddBookEvent.OnTitleChanged("T"))
        viewModel.onEvent(AddBookEvent.OnAuthorChanged("A"))
        coEvery { addBookUseCase(any()) } returns Result.success(Unit)
        
        viewModel.onEvent(AddBookEvent.OnSaveClick)
        
        coVerify { addBookUseCase(any()) }
        viewModel.effect.test {
            assertThat(awaitItem()).isEqualTo(UiEffect.NavigateBack)
        }
    }

    @Test
    fun `onEvent OnImportEpub updates draft with epub data`() = runTest {
        val uri = mockk<Uri>(relaxed = true)
        val book = Book(title = "Epub Title", author = "Epub Author", isPhysical = false)
        coEvery { getBookFromEpubUseCase(any(), any()) } returns book
        
        viewModel.uiState.test {
            skipItems(1)
            viewModel.onEvent(AddBookEvent.OnImportEpub(uri))
            awaitItem() // Loading
            val success = awaitItem()
            assertThat(success.bookDraft.title).isEqualTo("Epub Title")
        }
    }
}
