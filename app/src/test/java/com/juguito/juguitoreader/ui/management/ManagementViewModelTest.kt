package com.juguito.juguitoreader.ui.management

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.usecase.folder.DeleteFolderUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import com.juguito.juguitoreader.domain.usecase.genre.DeleteGenreUseCase
import com.juguito.juguitoreader.domain.usecase.genre.GetGenresUseCase
import com.juguito.juguitoreader.domain.usecase.genre.UpdateGenreUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
class ManagementViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ManagementViewModel
    private val getFoldersUseCase = mockk<GetFoldersUseCase>()
    private val getGenresUseCase = mockk<GetGenresUseCase>()
    private val deleteFolderUseCase = mockk<DeleteFolderUseCase>()
    private val deleteGenreUseCase = mockk<DeleteGenreUseCase>()
    private val updateGenreUseCase = mockk<UpdateGenreUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getFoldersUseCase() } returns flowOf(emptyList())
        every { getGenresUseCase() } returns flowOf(emptyList())
        viewModel = ManagementViewModel(getFoldersUseCase, getGenresUseCase, deleteFolderUseCase, deleteGenreUseCase, updateGenreUseCase)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEvent OnTabSelected updates state`() = runTest {
        viewModel.onEvent(ManagementEvent.OnTabSelected(1))
        assertThat(viewModel.uiState.value.selectedTab).isEqualTo(1)
    }

    @Test
    fun `onEvent OnDeleteFolder calls deleteFolderUseCase`() = runTest {
        coEvery { deleteFolderUseCase(1) } returns Result.success(Unit)
        viewModel.onEvent(ManagementEvent.OnDeleteFolder(1))
        coVerify { deleteFolderUseCase(1) }
    }

    @Test
    fun `onEvent OnDeleteGenre calls deleteGenreUseCase`() = runTest {
        coEvery { deleteGenreUseCase(1) } returns Result.success(Unit)
        viewModel.onEvent(ManagementEvent.OnDeleteGenre(1))
        coVerify { deleteGenreUseCase(1) }
    }

    @Test
    fun `onEvent genre editing updates state correctly`() = runTest {
        val genre = Genre(id = 1, name = "Sci-Fi")
        viewModel.onEvent(ManagementEvent.OnEditGenreClick(genre))
        
        assertThat(viewModel.uiState.value.genreToEdit).isEqualTo(genre)
        assertThat(viewModel.uiState.value.newGenreName).isEqualTo("Sci-Fi")
        
        viewModel.onEvent(ManagementEvent.OnGenreNameChanged("Sci-Fi Updated"))
        assertThat(viewModel.uiState.value.newGenreName).isEqualTo("Sci-Fi Updated")
        
        coEvery { updateGenreUseCase(any()) } returns Result.success(Unit)
        viewModel.onEvent(ManagementEvent.OnUpdateGenreConfirm)
        coVerify { updateGenreUseCase(match { it.name == "Sci-Fi Updated" }) }
        assertThat(viewModel.uiState.value.genreToEdit).isNull()
    }

    @Test
    fun `onEvent OnCancelEditGenre resets editing state`() = runTest {
        viewModel.onEvent(ManagementEvent.OnEditGenreClick(Genre(name = "G")))
        viewModel.onEvent(ManagementEvent.OnCancelEditGenre)
        assertThat(viewModel.uiState.value.genreToEdit).isNull()
    }

    @Test
    fun `onEvent OnSearchQueryChanged and OnToggleSearch update state`() = runTest {
        viewModel.onEvent(ManagementEvent.OnSearchQueryChanged("query"))
        assertThat(viewModel.uiState.value.searchQuery).isEqualTo("query")
        
        viewModel.onEvent(ManagementEvent.OnToggleSearch)
        assertThat(viewModel.uiState.value.isSearchActive).isTrue()
        
        viewModel.onEvent(ManagementEvent.OnToggleSearch)
        assertThat(viewModel.uiState.value.isSearchActive).isFalse()
        assertThat(viewModel.uiState.value.searchQuery).isEmpty()
    }
}
