package com.juguito.juguitoreader.ui.folder

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.usecase.folder.AddFolderUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFolderByIdUseCase
import com.juguito.juguitoreader.domain.usecase.folder.UpdateFolderUseCase
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FolderViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: FolderViewModel
    private val addFolderUseCase = mockk<AddFolderUseCase>()
    private val updateFolderUseCase = mockk<UpdateFolderUseCase>()
    private val getFolderByIdUseCase = mockk<GetFolderByIdUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state for creation has isEditing false`() = runTest {
        viewModel = FolderViewModel(addFolderUseCase, updateFolderUseCase, getFolderByIdUseCase, SavedStateHandle())
        assertThat(viewModel.uiState.value.isEditing).isFalse()
    }

    @Test
    fun `initial state for edition loads folder`() = runTest {
        val folder = Folder(id = 1, name = "My Folder", colorHex = "#000")
        coEvery { getFolderByIdUseCase(1) } returns folder
        
        viewModel = FolderViewModel(addFolderUseCase, updateFolderUseCase, getFolderByIdUseCase, SavedStateHandle(mapOf("folderId" to 1)))
        
        val state = viewModel.uiState.value
        assertThat(state.isEditing).isTrue()
        assertThat(state.name).isEqualTo("My Folder")
    }

    @Test
    fun `onEvent updates state fields`() = runTest {
        viewModel = FolderViewModel(addFolderUseCase, updateFolderUseCase, getFolderByIdUseCase, SavedStateHandle())
        
        viewModel.onEvent(FolderEvent.OnNameChanged("New Name"))
        viewModel.onEvent(FolderEvent.OnDescriptionChanged("Desc"))
        viewModel.onEvent(FolderEvent.OnColorChanged("#FFF"))
        
        val state = viewModel.uiState.value
        assertThat(state.name).isEqualTo("New Name")
        assertThat(state.description).isEqualTo("Desc")
        assertThat(state.colorHex).isEqualTo("#FFF")
    }

    @Test
    fun `onEvent OnSaveClick calls addFolderUseCase when not editing`() = runTest {
        viewModel = FolderViewModel(addFolderUseCase, updateFolderUseCase, getFolderByIdUseCase, SavedStateHandle())
        viewModel.onEvent(FolderEvent.OnNameChanged("New Folder"))
        coEvery { addFolderUseCase(any()) } returns Result.success(Unit)
        
        viewModel.onEvent(FolderEvent.OnSaveClick)
        
        coVerify { addFolderUseCase(match { it.name == "New Folder" }) }
        viewModel.effect.test {
            assertThat(awaitItem()).isEqualTo(UiEffect.NavigateBack)
        }
    }

    @Test
    fun `onEvent OnSaveClick calls updateFolderUseCase when editing`() = runTest {
        coEvery { getFolderByIdUseCase(any()) } returns Folder(id = 1, name = "Old", colorHex = "#000")
        viewModel = FolderViewModel(addFolderUseCase, updateFolderUseCase, getFolderByIdUseCase, SavedStateHandle(mapOf("folderId" to 1)))
        
        viewModel.onEvent(FolderEvent.OnNameChanged("Updated Name"))
        coEvery { updateFolderUseCase(any()) } returns Result.success(Unit)
        
        viewModel.onEvent(FolderEvent.OnSaveClick)
        
        coVerify { updateFolderUseCase(match { it.id == 1 && it.name == "Updated Name" }) }
    }

    @Test
    fun `DATA-017 OnSaveClick twice calls addFolder once`() = runTest {
        viewModel = FolderViewModel(addFolderUseCase, updateFolderUseCase, getFolderByIdUseCase, SavedStateHandle())
        viewModel.onEvent(FolderEvent.OnNameChanged("New Folder"))
        val latch = CompletableDeferred<Result<Unit>>()
        coEvery { addFolderUseCase(any()) } coAnswers { latch.await() }

        try {
            viewModel.onEvent(FolderEvent.OnSaveClick)
            viewModel.onEvent(FolderEvent.OnSaveClick)

            coVerify(exactly = 1) { addFolderUseCase(any()) }
        } finally {
            latch.complete(Result.success(Unit))
        }
    }
}
