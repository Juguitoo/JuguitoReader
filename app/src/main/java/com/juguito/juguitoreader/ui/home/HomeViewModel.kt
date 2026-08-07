package com.juguito.juguitoreader.ui.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.usecase.book.DeleteBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.book.ImportBookFromUriUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val getBooksUseCase: GetBooksUseCase,
    private val importBookFromUriUseCase: ImportBookFromUriUseCase,
    private val deleteBookUseCase: DeleteBookUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getBooksUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar los libros: ${exception.localizedMessage}"
                    )
                }
                .collect { books ->
                    val filteredBooks = books.sortedWith(
                        compareByDescending<Book> { it.status == BookStatus.READING }
                            .thenByDescending { it.createdAt }
                    ).take(10)

                    val newStats = StatsUiState(
                        totalBooks = books.size,
                        readingBooks = books.count { it.status == BookStatus.READING },
                        finishedBooks = books.count {it.status == BookStatus.FINISHED},
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recentBooks = filteredBooks,
                        stats = newStats,
                        errorMessage = null
                    )
                }
        }
    }

    fun importBook(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = importBookFromUriUseCase(application, uri)
            
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al importar el libro: ${exception.localizedMessage}"
                )
            }
        }
    }

    fun deleteBook(bookId: Int) {
        viewModelScope.launch {
            deleteBookUseCase(bookId)
        }
    }
}
