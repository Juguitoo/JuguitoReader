package com.juguito.juguitoreader.ui.book.add

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.usecase.book.AddBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBookFromEpubUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import com.juguito.juguitoreader.domain.usecase.genre.GetGenresUseCase
import com.juguito.juguitoreader.ui.common.UiText
import com.juguito.juguitoreader.ui.common.asUiText
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.utils.FileUtils
import com.juguito.juguitoreader.utils.FileUtils.deleteFileFromInternalStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val application: Application,
    private val addBookUseCase: AddBookUseCase,
    private val getFoldersUseCase: GetFoldersUseCase,
    private val getGenresUseCase: GetGenresUseCase,
    private val getBookFromEpubUseCase: GetBookFromEpubUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddBookUiState())
    val uiState: StateFlow<AddBookUiState> = _uiState.asStateFlow()

    private val _effect = Channel<UiEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadAvailableData()
    }

    private fun loadAvailableData(){
        viewModelScope.launch {
            getFoldersUseCase()
                .catch { error ->
                    _effect.send(UiEffect.ShowSnackbar(error.asUiText()))
                }
                .collect { foldersFromDb ->
                _uiState.value = _uiState.value.copy(
                    availableFolders = foldersFromDb
                )
            }
        }
        viewModelScope.launch {
            getGenresUseCase()
                .catch { error ->
                    _effect.send(UiEffect.ShowSnackbar(error.asUiText()))
                }
                .collect { genresFromDb ->
                _uiState.value = _uiState.value.copy(
                    availableGenres = genresFromDb
                )
            }
        }
    }

    fun onEvent(event: AddBookEvent) {
        when (event) {
            is AddBookEvent.OnTitleChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(title = event.title)
                )
            }
            is AddBookEvent.OnAuthorChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(author = event.author)
                )
            }
            is AddBookEvent.OnPublisherChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(publisher = event.publisher)
                )
            }
            is AddBookEvent.OnSeriesChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(series = event.series)
                )
            }
            is AddBookEvent.OnSeriesOrderChanged -> {
                val sanitizedInput = event.seriesOrder.replace(',', '.')
                val decimalRegex = Regex("""^\d{0,2}(\.\d{0,2})?$""")
                if (decimalRegex.matches(sanitizedInput)) {
                    _uiState.value = _uiState.value.copy(
                        bookDraft = _uiState.value.bookDraft.copy(seriesOrder = sanitizedInput)
                    )
                }
            }
            is AddBookEvent.OnIsPhysicalChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(isPhysical = event.isPhysical)
                )
            }
            is AddBookEvent.OnCoverChanged -> {
                val previousCover = _uiState.value.bookDraft.coverUrl
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(coverUrl = event.coverPath)
                )
                viewModelScope.launch(Dispatchers.IO) {
                    FileUtils.deleteStagingAsset(application, previousCover)
                }
            }
            is AddBookEvent.OnEpubFilePicked -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(localFilePath = event.uri.toString())
                )
            }
            is AddBookEvent.OnLocalFilePathChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(localFilePath = event.localFilePath)
                )
            }
            is AddBookEvent.OnFoldersChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(folders = event.folders)
                )
            }
            is AddBookEvent.OnGenresChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(genres = event.genres)
                )
            }
            is AddBookEvent.OnImportEpub -> {
                importEpubData(event.uri)
            }
            AddBookEvent.OnSaveClick -> {
                saveBook()
            }
            AddBookEvent.OnDiscard -> {
                val cover = _uiState.value.bookDraft.coverUrl
                viewModelScope.launch(Dispatchers.IO) {
                    FileUtils.deleteStagingAsset(application, cover)
                }
            }
        }
    }

    private fun saveBook() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val draft = currentState.bookDraft

            if (draft.title.isBlank()) {
                _effect.send(UiEffect.ShowSnackbar(UiText.StringResource(R.string.error_title_empty)))
                return@launch
            }
            if (draft.author.isBlank()) {
                _effect.send(UiEffect.ShowSnackbar(UiText.StringResource(R.string.error_author_empty)))
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val filesResult = withContext(Dispatchers.IO) {
                    FileUtils.promotePendingFiles(
                        application,
                        if (draft.isPhysical) null else draft.localFilePath,
                        draft.coverUrl,
                    )
                }

                val newBook = Book(
                    title = draft.title,
                    author = draft.author,
                    publisher = draft.publisher,
                    series = draft.series,
                    seriesOrder = draft.seriesOrder.toDoubleOrNull(),
                    isPhysical = draft.isPhysical,
                    coverUrl = filesResult.coverPath,
                    localFilePath = if (draft.isPhysical) null else filesResult.epubPath,
                    folders = draft.folders,
                    genres = draft.genres,
                )

                val result = addBookUseCase(newBook)

                result.fold(
                    onSuccess = {
                        withContext(Dispatchers.IO) {
                            FileUtils.deleteStagingAsset(application, draft.coverUrl)
                        }
                        _effect.send(UiEffect.NavigateBack)
                    },
                    onFailure = { exception ->
                        withContext(Dispatchers.IO) {
                            if (filesResult.epubPath != null) deleteFileFromInternalStorage(
                                application,
                                filesResult.epubPath
                            )
                            if (filesResult.coverPath != null) deleteFileFromInternalStorage(
                                application,
                                filesResult.coverPath
                            )
                        }
                        _effect.send(UiEffect.ShowSnackbar(exception.asUiText()))
                    }
                )
            } catch (e: JuguitoException) {
                _effect.send(UiEffect.ShowSnackbar(UiText.StringResource(e.resId)))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun importEpubData(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val previousCover = _uiState.value.bookDraft.coverUrl
                _uiState.value = _uiState.value.copy(isLoading = true)
                val book = getBookFromEpubUseCase(application, uri, false)
                withContext(Dispatchers.IO) {
                    FileUtils.deleteStagingAsset(application, previousCover)
                }

                val currentDraft = _uiState.value.bookDraft
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    bookDraft = currentDraft.copy(
                        title = book.title.ifBlank { currentDraft.title },
                        author = book.author.ifBlank { currentDraft.author },
                        publisher = book.publisher ?: currentDraft.publisher,
                        series = book.series ?: currentDraft.series,
                        seriesOrder = book.seriesOrder?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: currentDraft.seriesOrder,
                        coverUrl = book.coverUrl ?: currentDraft.coverUrl,
                        localFilePath = book.localFilePath,
                        genres = (book.genres + currentDraft.genres).distinctBy { it.name.lowercase() }
                    )
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _effect.send(
                    UiEffect.ShowSnackbar(
                        message = UiText.StringResource(R.string.import_epub_successfull)
                    )
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _effect.send(
                    UiEffect.ShowSnackbar(
                        message = UiText.StringResource(R.string.something_went_wrong)
                    )
                )
            }
        }
    }
}
