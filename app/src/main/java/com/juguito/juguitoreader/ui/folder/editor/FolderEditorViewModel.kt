package com.juguito.juguitoreader.ui.folder.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.usecase.folder.AddFolderUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFolderByIdUseCase
import com.juguito.juguitoreader.domain.usecase.folder.UpdateFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderEditorViewModel @Inject constructor(
    private val addFolderUseCase: AddFolderUseCase,
    private val updateFolderUseCase: UpdateFolderUseCase,
    private val getFolderByIdUseCase: GetFolderByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(FolderEditorUiState())
    val uiState: StateFlow<FolderEditorUiState> = _uiState.asStateFlow()

    private var currentFolderId: Int = 0

    init {
        val folderId: Int? = savedStateHandle["folderId"]
        if (folderId != null && folderId != 0) {
            currentFolderId = folderId
            loadFolder(folderId)
        }
    }

    private fun loadFolder(id: Int) {
        viewModelScope.launch {
            val folder = getFolderByIdUseCase(id)
            if (folder != null) {
                _uiState.value = _uiState.value.copy(
                    name = folder.name,
                    description = folder.description ?: "",
                    colorHex = folder.colorHex,
                    isEditing = true
                )
            }
        }
    }

    fun onEvent(event: FolderEditorEvent) {
        when (event) {
            is FolderEditorEvent.OnNameChanged -> {
                _uiState.value = _uiState.value.copy(name = event.name)
            }
            is FolderEditorEvent.OnColorChanged -> {
                _uiState.value = _uiState.value.copy(colorHex = event.colorHex)
            }
            is FolderEditorEvent.OnDescriptionChanged -> {
                _uiState.value = _uiState.value.copy(description = event.description)
            }
            FolderEditorEvent.OnSaveClick -> {
                saveFolder()
            }
        }
    }

    private fun saveFolder() {
        val currentState = _uiState.value

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val folder = Folder(
                id = currentFolderId,
                name = currentState.name,
                description = currentState.description,
                colorHex =  currentState.colorHex
            )

            val result = if (currentFolderId == 0) {
                addFolderUseCase(folder)
            } else {
                updateFolderUseCase(folder)
            }

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSaved = true
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage
                    )
                }
            )
        }
    }
}
