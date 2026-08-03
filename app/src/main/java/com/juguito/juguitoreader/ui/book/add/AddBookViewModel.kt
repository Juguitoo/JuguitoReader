package com.juguito.juguitoreader.ui.book.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.usecase.book.AddBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val addBookUseCase: AddBookUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddBookUiState())
    val uiState: StateFlow<AddBookUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddBookEvent) {
        when (event) {
            is AddBookEvent.OnTitleChanged -> {
                _uiState.value = _uiState.value.copy(title = event.title)
            }
            is AddBookEvent.OnAuthorChanged -> {
                _uiState.value = _uiState.value.copy(author = event.author)
            }
            is AddBookEvent.OnPublisherChanged -> {
                _uiState.value = _uiState.value.copy(publisher = event.publisher)
            }
            is AddBookEvent.OnIsPhysicalChanged -> {
                _uiState.value = _uiState.value.copy(isPhysical = event.isPhysical)
            }
            is AddBookEvent.OnStatusChanged -> {
                _uiState.value = _uiState.value.copy(status = event.status)
            }
            is AddBookEvent.OnRatingChanged -> {
                _uiState.value = _uiState.value.copy(rating = event.rating)
            }
            is AddBookEvent.OnStartDateChanged -> {
                _uiState.value = _uiState.value.copy(startDate = event.startDate)
            }
            is AddBookEvent.OnEndDateChanged -> {
                _uiState.value = _uiState.value.copy(endDate = event.endDate)
            }
            is AddBookEvent.OnCoverUrlChanged -> {
                _uiState.value = _uiState.value.copy(coverUrl = event.coverUrl)
            }
            is AddBookEvent.OnLocalFilePathChanged -> {
                _uiState.value = _uiState.value.copy(localFilePath = event.localFilePath)
            }
            is AddBookEvent.OnFoldersChanged -> {
                _uiState.value = _uiState.value.copy(folders = event.folders)
            }
            is AddBookEvent.OnGenresChanged -> {
                _uiState.value = _uiState.value.copy(genres = event.genres)
            }
            AddBookEvent.OnSaveClick -> {
                saveBook()
            }
        }
    }

    private fun saveBook() {
        val currentState = _uiState.value

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val newBook = Book(
                title = currentState.title,
                author = currentState.author,
                publisher = currentState.publisher,
                isPhysical = currentState.isPhysical,
                status = currentState.status,
                rating = currentState.rating,
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                coverUrl = currentState.coverUrl,
                localFilePath = currentState.localFilePath,
                folders = currentState.folders,
                genres = currentState.genres,
            )

            val result = addBookUseCase(newBook)

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