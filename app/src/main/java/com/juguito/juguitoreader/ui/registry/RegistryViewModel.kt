package com.juguito.juguitoreader.ui.registry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.BookCriteria
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.applyCriteria
import com.juguito.juguitoreader.domain.model.copy
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
import com.juguito.juguitoreader.ui.common.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistryViewModel @Inject constructor(
    private val application: Application,
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
                    exception.printStackTrace()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = UiText.StringResource(R.string.something_went_wrong).asString(application)
                    )
                }
                .collect { books ->
                    val filtered = books.applyCriteria(_uiState.value.criteria)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        books = books,
                        filteredBooks = filtered
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
                updateBookField(event.bookId) { 
                    it.copy(
                        endDate = event.endDate,
                        status = if (event.endDate != null) BookStatus.FINISHED else it.status
                    )
                }
            }
            is RegistryEvent.OnCommentChanged -> {
                updateBookField(event.bookId) { it.copy(comment = event.comment) }
            }
            
            // Eventos de Filtrado
            is RegistryEvent.OnSearchTextChanged -> {
                val newCriteria = _uiState.value.criteria.copy(searchText = event.text)
                updateCriteria(newCriteria)
            }
            is RegistryEvent.OnToggleSearch -> {
                _uiState.value = _uiState.value.copy(
                    isSearchExpanded = event.expanded,
                    criteria = if (!event.expanded) _uiState.value.criteria.copy(searchText = "") else _uiState.value.criteria
                )
                if (!event.expanded) updateCriteria(_uiState.value.criteria)
            }
            is RegistryEvent.OnShowFilterSheet -> {
                _uiState.value = _uiState.value.copy(showFilterSheet = event.show)
            }
            is RegistryEvent.OnCriteriaChanged -> {
                updateCriteria(event.criteria)
            }
            RegistryEvent.OnClearFilters -> {
                updateCriteria(BookCriteria())
            }
        }
    }

    private fun updateCriteria(criteria: BookCriteria) {
        val filtered = _uiState.value.books.applyCriteria(criteria)
        _uiState.value = _uiState.value.copy(
            criteria = criteria,
            filteredBooks = filtered
        )
    }

    private fun updateBookField(bookId: Int, updateLogic: (Book) -> Book) {
        val book = _uiState.value.books.find { it.id == bookId } ?: return
        val updatedBook = updateLogic(book)

        viewModelScope.launch {
            updateBookUseCase(updatedBook)
        }
    }
}
