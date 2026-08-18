package com.juguito.juguitoreader.ui.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.copy
import com.juguito.juguitoreader.domain.usecase.folder.DeleteFolderUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import com.juguito.juguitoreader.domain.usecase.genre.DeleteGenreUseCase
import com.juguito.juguitoreader.domain.usecase.genre.GetGenresUseCase
import com.juguito.juguitoreader.domain.usecase.genre.UpdateGenreUseCase
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagementViewModel @Inject constructor(
    private val getFoldersUseCase: GetFoldersUseCase,
    private val getGenresUseCase: GetGenresUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val deleteGenreUseCase: DeleteGenreUseCase,
    private val updateGenreUseCase: UpdateGenreUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagementUiState())
    val uiState: StateFlow<ManagementUiState> = _uiState.asStateFlow()

    private val _effect = Channel<UiEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadFolders()
        loadGenres()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            getFoldersUseCase().collect { foldersList ->
                _uiState.value = _uiState.value.copy(folders = foldersList)
            }
        }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            getGenresUseCase().collect { genresList ->
                _uiState.value = _uiState.value.copy(genres = genresList)
            }
        }
    }

    fun onEvent(event: ManagementEvent) {
        when (event) {
            is ManagementEvent.OnTabSelected -> {
                _uiState.value = _uiState.value.copy(selectedTab = event.index)
            }
            is ManagementEvent.OnDeleteFolder -> {
                deleteFolder(event.folderId)
            }
            is ManagementEvent.OnDeleteGenre -> {
                deleteGenre(event.genreId)
            }
            is ManagementEvent.OnEditGenreClick -> {
                _uiState.value = _uiState.value.copy(
                    genreToEdit = event.genre,
                    newGenreName = event.genre.name
                )
            }
            is ManagementEvent.OnGenreNameChanged -> {
                _uiState.value = _uiState.value.copy(newGenreName = event.name)
            }
            is ManagementEvent.OnSearchQueryChanged -> {
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
            }
            is ManagementEvent.OnToggleSearch -> {
                val isSearchActive = !_uiState.value.isSearchActive
                _uiState.value = _uiState.value.copy(
                    isSearchActive = isSearchActive,
                    searchQuery = if (!isSearchActive) "" else _uiState.value.searchQuery
                )
            }
            is ManagementEvent.OnCancelEditGenre -> {
                _uiState.value = _uiState.value.copy(genreToEdit = null, newGenreName = "")
            }
            is ManagementEvent.OnUpdateGenreConfirm -> {
                updateGenre()
            }
        }
    }

    private fun deleteFolder(id: Int) {
        viewModelScope.launch {
            deleteFolderUseCase(id)
        }
    }

    private fun deleteGenre(id: Int) {
        viewModelScope.launch {
            deleteGenreUseCase(id)
        }
    }

    private fun updateGenre() {
        val genreToEdit = _uiState.value.genreToEdit ?: return
        val newName = _uiState.value.newGenreName
        
        if (newName.isBlank()) return

        viewModelScope.launch {
            val updatedGenre = genreToEdit.copy(name = newName)
            val result = updateGenreUseCase(updatedGenre)
            
            result.onSuccess {
                _uiState.value = _uiState.value.copy(genreToEdit = null, newGenreName = "")
            }.onFailure { exception ->
                exception.printStackTrace()
                _effect.send(UiEffect.ShowSnackbar(exception.message ?: "Error al actualizar el género."))
            }
        }
    }
}
