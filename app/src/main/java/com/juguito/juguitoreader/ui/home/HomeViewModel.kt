package com.juguito.juguitoreader.ui.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.common.ActionUndoManager
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.usecase.book.AddBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.DeleteBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.book.ImportBookFromUriUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.GetReadingProgressesUseCase
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getBooksUseCase: GetBooksUseCase,
    private val getReadingProgressesUseCase: GetReadingProgressesUseCase,
    private val importBookFromUriUseCase: ImportBookFromUriUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val addBookUseCase: AddBookUseCase
) : ViewModel() {

    private val _internalState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _internalState.asStateFlow()

    private val _effect = Channel<UiEffect>()
    val effect = _effect.receiveAsFlow()

    private val bookUndoManager = ActionUndoManager<Book>()

    init {
        loadData()
    }

    private fun loadData() {
        _internalState.value = HomeUiState.Loading

        viewModelScope.launch {
            combine(
                getBooksUseCase(),
                getReadingProgressesUseCase()
            ) { books, progresses ->

                if (books.isEmpty()) return@combine HomeUiState.Empty as HomeUiState

                val newStats = StatsUiState(
                    totalBooksCount = books.size,
                    readingBooksCount = books.count { it.status == BookStatus.READING },
                    finishedBooksCount = books.count { it.status == BookStatus.FINISHED },
                )

                val availableBooks = books.filter { !it.isPhysical && !it.localFilePath.isNullOrBlank() }

                val progressMap = progresses.associateBy { it.bookId }
                val pendingBooks = availableBooks
                    .filter { it.status == BookStatus.PENDING }
                    .sortedByDescending { it.createdAt }
                    .take(10)
                val recentBooks = availableBooks
                    .filter { progressMap.containsKey(it.id) && it.status == BookStatus.READING }
                    .sortedByDescending { progressMap[it.id]?.lastReadAt ?: it.createdAt }
                    .take(10)

                HomeUiState.Success(
                    readingBooks = recentBooks,
                    pendingBooks = pendingBooks,
                    stats = newStats
                ) as HomeUiState
            }.catch { exception ->
                emit(
                    HomeUiState.Error(
                        message = "Error al cargar los datos: ${exception.localizedMessage}"
                    )
                )
            }.collect { newState ->
                _internalState.value = newState
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnDeleteBookClick -> {deleteBook(event.book)}
            is HomeEvent.OnDeleteConfirmed -> {
                viewModelScope.launch { bookUndoManager.confirmPending() }
            }
            is HomeEvent.OnUndoDeleteClick -> {
                viewModelScope.launch { bookUndoManager.undoPending() }
            }
            is HomeEvent.OnDismissError -> {
                viewModelScope.launch { dismissError() }
            }
            is HomeEvent.OnImportBook -> {
                importBook(event.uri)
            }
        }
    }

    fun importBook(uri: Uri) {
        _internalState.value = HomeUiState.Loading
        viewModelScope.launch {
            val result = importBookFromUriUseCase(context, uri)

            result.onSuccess {
            }.onFailure { exception ->
                exception.printStackTrace()
                _effect.send(UiEffect.ShowSnackbar("Error al importar los datos."))
            }
        }
    }

    fun dismissError() {
        loadData()
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            bookUndoManager.executeAction(
                item = book,
                immediateAction = { item ->
                    val result = deleteBookUseCase(item.id)
                    result.fold(
                        onSuccess = {
                            _effect.send(
                                UiEffect.ShowSnackbar(
                                    message = "Libro eliminado.",
                                    actionLabel = "Deshacer"
                                )
                            )
                            true
                        },
                        onFailure = { error ->
                            _effect.send(
                                UiEffect.ShowSnackbar(
                                    message = error.message ?: "Error al eliminar el libro."
                                )
                            )
                            false
                        }
                    )
                },
                onConfirm = { item ->
                    item.coverUrl?.let { path ->
                        FileUtils.deleteFileFromInternalStorage(context, path)
                    }
                    item.localFilePath?.let { path ->
                        FileUtils.deleteFileFromInternalStorage(context, path)
                    }
                },
                onUndo = {item ->
                    addBookUseCase(item)
                }
            )
        }
    }
}
