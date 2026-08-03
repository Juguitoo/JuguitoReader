package com.juguito.juguitoreader.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecentBooks()
    }

    private fun loadRecentBooks() {

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
                        recentBooks = books.sortedByDescending { it.createdAt }.take(5),
                        errorMessage = null
                    )
                }
        }
    }
}