package com.juguito.juguitoreader.ui.folder.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.usecase.folder.AddFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFolderViewModel @Inject constructor(
    private val addFolderUseCase: AddFolderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddFolderUiState())
    val uiState: StateFlow<AddFolderUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddFolderEvent) {
        when (event) {
            is AddFolderEvent.OnNameChanged -> {
                _uiState.value = _uiState.value.copy(name = event.name)
            }
            is AddFolderEvent.OnColorChanged -> {
                _uiState.value = _uiState.value.copy(colorHex = event.colorHex)
            }
            is AddFolderEvent.OnDescriptionChanged -> {
                _uiState.value = _uiState.value.copy(description = event.description)
            }
            AddFolderEvent.OnSaveClick -> {
                saveFolder()
            }
        }
    }

    private fun saveFolder() {
        val currentState = _uiState.value

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val newFolder = Folder(
                name = currentState.name,
                description = currentState.description,
                colorHex =  currentState.description
            )

            val result = addFolderUseCase(newFolder)

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