package com.juguito.juguitoreader.ui.registry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.copy
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistryViewModel @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase,
    private val updateBookUseCase: UpdateBookUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistryUiState())
    val uiState: StateFlow<RegistryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(isLoading = true)

            getBooksUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar los libros: ${exception.localizedMessage}"
                    )
                }
                .collect { books ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        books = books
                    )
                }
        }
    }

    fun onEvent(event: RegistryEvent) {
        when (event) {
            is RegistryEvent.OnRatingChanged -> {
                updateBookField(event.bookId) { it.copy(rating = event.rating) }
            }
            is RegistryEvent.OnStatusChanged -> {
                updateBookField(event.bookId) { it.copy(status = event.status) }
            }
            is RegistryEvent.OnStartDateChanged -> {
                updateBookField(event.bookId) { it.copy(startDate = event.startDate) }
            }
            is RegistryEvent.OnEndDateChanged -> {
                updateBookField(event.bookId) { it.copy(endDate = event.endDate) }
            }
            is RegistryEvent.OnCommentChanged -> {
                updateBookField(event.bookId) {it.copy(comment = event.comment)}
            }
        }
    }

    private fun updateBookField(bookId: Int, updateLogic: (Book) -> Book) {
        val book = _uiState.value.books.find {it.id == bookId} ?: return
        val updatedBook = updateLogic(book)

        viewModelScope.launch {
            updateBookUseCase(updatedBook)
        }
    }
}
