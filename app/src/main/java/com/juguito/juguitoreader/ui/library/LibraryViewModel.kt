package com.juguito.juguitoreader.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase,
    private val getFoldersUseCase: GetFoldersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            combine(
                getFoldersUseCase(),
                getBooksUseCase()
            ) { folders, books ->
                _uiState.value = _uiState.value.copy(
                    folders = folders,
                    allBooks = books,
                    isLoading = false
                )
            }.catch { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage
                )
            }.collect()
        }
    }

    fun onEvent(event: LibraryEvent) {
        when (event) {
            is LibraryEvent.OnSelectedFolderChanged -> {
                _uiState.value = _uiState.value.copy(
                    selectedFolder = event.selectedFolder
                )
            }
            is LibraryEvent.OnSearchTextChanged -> {
                _uiState.value = _uiState.value.copy(
                    searchText = event.searchText?.trim() ?: ""
                )
            }
        }
    }

    fun readBook(book: Book) {

    }


}