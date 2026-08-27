package com.juguito.juguitoreader.ui.book.detail

import android.app.Application
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.usecase.book.DeleteBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBookByIdUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBookFromEpubUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
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
class BookDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var viewModel: BookDetailViewModel
    private val application = mockk<Application>(relaxed = true)
    private val getBookByIdUseCase = mockk<GetBookByIdUseCase>()
    private val updateBookUseCase = mockk<UpdateBookUseCase>()
    private val getBookFromEpubUseCase = mockk<GetBookFromEpubUseCase>()
    private val getFoldersUseCase = mockk<GetFoldersUseCase>()
    private val getGenresUseCase = mockk<GetGenresUseCase>()
    private val deleteBookUseCase = mockk<DeleteBookUseCase>()
    private val savedStateHandle = SavedStateHandle(mapOf("bookId" to 1))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockkStatic(Uri::class)
        val uri = mockk<Uri>(relaxed = true)
        every { Uri.parse(any()) } returns uri
        
        mockkObject(FileUtils)
        
        every { getFoldersUseCase() } returns flowOf(emptyList())
        every { getGenresUseCase() } returns flowOf(emptyList())
        coEvery { getBookByIdUseCase(1) } returns Book(id = 1, title = "Title", author = "Author", isPhysical = true)
        
        viewModel = BookDetailViewModel(
            application,
            getBookByIdUseCase,
            updateBookUseCase,
            getBookFromEpubUseCase,
            getFoldersUseCase,
            getGenresUseCase,
            deleteBookUseCase,
            savedStateHandle
        )
    }

    @After
    fun teardown() {
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
            assertThat(success.bookDraft.title).isEqualTo("Title") // Original
        }
    }

    @Test
    fun `onEvent OnCoverUrlChanged updates cover path`() = runTest {
        every { FileUtils.saveImageToInternalStorage(any(), any()) } returns "permanent/path"
        
        viewModel.onEvent(BookDetailEvent.OnCoverUrlChanged("temp/path"))
        
        viewModel.uiState.test {
            val success = awaitItem() as BookDetailUiState.Success
            assertThat(success.bookDraft.coverUrl).isEqualTo("permanent/path")
        }
    }

    @Test
    fun `onEvent OnSaveClick calls updateBookUseCase and sends success effect`() = runTest {
        coEvery { updateBookUseCase(any()) } returns Result.success(Unit)
        
        viewModel.onEvent(BookDetailEvent.OnSaveClick)
        
        coVerify { updateBookUseCase(any()) }
        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            val uiText = effect.message as UiText.StringResource
            assertThat(effect).isInstanceOf(UiEffect.ShowSnackbar::class.java)
            assertEquals(R.string.save_success, uiText.resId)
        }
    }

    @Test
    fun `onEvent OnDeleteClick calls deleteBookUseCase and navigates back`() = runTest {
        coEvery { deleteBookUseCase(1) } returns Result.success(Unit)
        
        viewModel.onEvent(BookDetailEvent.OnDeleteClick)
        
        coVerify { deleteBookUseCase(1) }
        viewModel.effect.test {
            val effect = awaitItem()
            assertThat(effect).isEqualTo(UiEffect.NavigateBack)
        }
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
}
