package com.juguito.juguitoreader.ui.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.usecase.book.DeleteBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.book.ImportBookFromUriUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.GetReadingProgressesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val getBooksUseCase: GetBooksUseCase,
    private val getReadingProgressesUseCase: GetReadingProgressesUseCase,
    private val importBookFromUriUseCase: ImportBookFromUriUseCase,
    private val deleteBookUseCase: DeleteBookUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            combine(
                getBooksUseCase(),
                getReadingProgressesUseCase()
            ) { books, progresses ->

                val newStats = StatsUiState(
                    totalBooks = books.size,
                    readingBooks = books.count { it.status == BookStatus.READING },
                    finishedBooks = books.count { it.status == BookStatus.FINISHED },
                )

                val progressMap = progresses.associateBy { it.bookId }
                val recentBooks = books.filter { book ->
                        progressMap.containsKey(book.id)
                    }.sortedByDescending { book ->
                        progressMap[book.id]?.lastReadAt ?: book.createdAt
                    }.take(5)

                _uiState.value.copy(
                    isLoading = false,
                    recentBooks = recentBooks,
                    stats = newStats,
                    errorMessage = null
                )
            }.catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar los datos: ${exception.localizedMessage}"
                    )
            }.collect { newState ->
                    _uiState.value = newState
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
