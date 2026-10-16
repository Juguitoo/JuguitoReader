package com.juguito.juguitoreader.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.ui.common.ObserveAsEvents
import com.juguito.juguitoreader.ui.common.components.JuguitoTopBar
import com.juguito.juguitoreader.ui.common.UiText
import com.juguito.juguitoreader.ui.common.components.EmptyLibraryView
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.components.ErrorView
import com.juguito.juguitoreader.ui.home.components.ActiveReadingsSection
import com.juguito.juguitoreader.ui.home.components.AddOrImportBooksRow
import com.juguito.juguitoreader.ui.home.components.FavouriteBooksSection
import com.juguito.juguitoreader.ui.home.components.FeaturedReading
import com.juguito.juguitoreader.ui.home.components.HomeSectionHeader
import com.juguito.juguitoreader.ui.home.components.PendingBookRow
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import kotlinx.coroutines.flow.Flow

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    snackbarMessage: Int?,
    onClearSnackbarMessage: () -> Unit,
    onNavigateToAddBook: () -> Unit,
    onNavigateToBookDetail: (Int) -> Unit,
    onNavigateToReadBook: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onViewActiveReadings: () -> Unit = {},
    onViewFavourites: () -> Unit = {},
    onViewPending: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.uiState.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val pendingUndoBookId by viewModel.pendingUndoBookId.collectAsState()

    HomeContent(
        modifier = modifier,
        state = homeState,
        isImporting = isImporting,
        pendingUndoBookId = pendingUndoBookId,
        snackbarMessage = snackbarMessage,
        onClearSnackbarMessage = onClearSnackbarMessage,
        onNavigateToAddBook = onNavigateToAddBook,
        onNavigateToBookDetail = onNavigateToBookDetail,
        onNavigateToReadBook = onNavigateToReadBook,
        onNavigateToSettings = onNavigateToSettings,
        onViewActiveReadings = onViewActiveReadings,
        onViewFavourites = onViewFavourites,
        onViewPending = onViewPending,
        onEvent = viewModel::onEvent,
        effect = viewModel.effect
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    state: HomeUiState,
    isImporting: Boolean = false,
    pendingUndoBookId: Int? = null,
    snackbarMessage: Int?,
    onClearSnackbarMessage: () -> Unit,
    onNavigateToAddBook: () -> Unit,
    onNavigateToBookDetail: (Int) -> Unit,
    onNavigateToReadBook: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onViewActiveReadings: () -> Unit = {},
    onViewFavourites: () -> Unit = {},
    onViewPending: () -> Unit = {},
    onEvent: (HomeEvent) -> Unit,
    effect: Flow<UiEffect>
) {
    val context = LocalContext.current
    var bookToDelete by remember { mutableStateOf<Book?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val bookDeletedMessage = stringResource(R.string.book_deleted)
    val undoLabel = stringResource(R.string.undo)

    LaunchedEffect(bookToDelete) {
        bookToDelete?.let {
            onEvent(HomeEvent.OnDeleteBookClick(it))
        }
        bookToDelete = null
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(UiText.StringResource(message).asString(context))
            onClearSnackbarMessage()
        }
    }

    LaunchedEffect(pendingUndoBookId) {
        val bookId = pendingUndoBookId ?: return@LaunchedEffect

        val result = snackbarHostState.showSnackbar(
            message = bookDeletedMessage,
            actionLabel = undoLabel,
            duration = SnackbarDuration.Short
        )

        when (result) {
            SnackbarResult.ActionPerformed -> onEvent(HomeEvent.OnUndoDeleteClick(bookId))
            SnackbarResult.Dismissed -> onEvent(HomeEvent.OnDeleteConfirmed(bookId))
        }
    }

    ObserveAsEvents(effect) { uiEffect ->
        when (uiEffect) {
            is UiEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(
                    message = uiEffect.message.asString(context),
                    actionLabel = uiEffect.actionLabel?.asString(context),
                    duration = SnackbarDuration.Short
                )
            }
            else -> Unit
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                onEvent(HomeEvent.OnImportBook(it))
            }
        }
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        },
        topBar = {
            JuguitoTopBar(
                centerAligned = true,
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            stringResource(R.string.juguito_label),
                            style = MaterialTheme.typography.titleLarge,
                            fontStyle = FontStyle.Italic,
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(R.string.reader_label),
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToSettings() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_label)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is HomeUiState.Error -> {
                    ErrorView(
                        message = state.message.asString(),
                        onRetry = { onEvent(HomeEvent.OnDismissError) }
                    )
                }
                is HomeUiState.Empty -> {
                    EmptyLibraryView(
                        onNavigateToAddBook = onNavigateToAddBook,
                        onImportClick = {
                            if (!isImporting) {
                                documentPickerLauncher.launch(
                                    arrayOf(
                                        "application/epub+zip"
                                    )
                                )
                            }
                        }
                    )
                }
                is HomeUiState.Success -> {
                    val hasReading = state.readingBooks.isNotEmpty()
                    val hasPending = state.pendingBooks.isNotEmpty()
                    val hasFavourites = state.favouriteBooks.isNotEmpty()

                    if (!hasReading && !hasPending && !hasFavourites) {
                        EmptyLibraryView(
                            onNavigateToAddBook = onNavigateToAddBook,
                            onImportClick = {
                                if (!isImporting) {
                                    documentPickerLauncher.launch(
                                        arrayOf(
                                            "application/epub+zip"
                                        )
                                    )
                                }
                            },
                            title = stringResource(R.string.empty_home_readable_title),
                            subtitle = stringResource(R.string.empty_home_readable_subtitle)
                        )
                    } else {
                        val featuredBook = state.readingBooks.firstOrNull()
                        val otherReadingBooks = state.readingBooks.drop(1)
                        val openBook: (Book) -> Unit = { book ->
                            val canRead = !book.isPhysical && !book.localFilePath.isNullOrBlank()
                            if (canRead) onNavigateToReadBook(book.id) else onNavigateToBookDetail(book.id)
                        }

                        LazyColumn(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .widthIn(max = 840.dp)
                                .fillMaxHeight()
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp)
                        ) {
                            if (featuredBook != null) {
                                item(key = "featured", contentType = "featured") {
                                    FeaturedReading(
                                        book = featuredBook,
                                        onOpen = { openBook(featuredBook) },
                                        onRead = { onNavigateToReadBook(featuredBook.id) },
                                        onDetails = { onNavigateToBookDetail(featuredBook.id) },
                                        onDelete = { bookToDelete = featuredBook },
                                    )
                                }
                            }

                            if (otherReadingBooks.isNotEmpty()) {
                                item(key = "active-reading", contentType = "book-row") {
                                    ActiveReadingsSection(
                                        books = otherReadingBooks,
                                        onOpenBook = openBook,
                                        onReadBook = onNavigateToReadBook,
                                        onBookDetails = onNavigateToBookDetail,
                                        onDeleteBook = { bookToDelete = it },
                                        onViewAll = onViewActiveReadings
                                    )
                                }
                            }

                            if (hasFavourites) {
                                item(key = "favourites", contentType = "book-row") {
                                    FavouriteBooksSection(
                                        books = state.favouriteBooks,
                                        onOpenBook = openBook,
                                        onReadBook = onNavigateToReadBook,
                                        onBookDetails = onNavigateToBookDetail,
                                        onDeleteBook = { bookToDelete = it },
                                        onViewCollection = onViewFavourites
                                    )
                                }
                            }

                            if (hasPending) {
                                item(key = "pending-header", contentType = "header") {
                                    HomeSectionHeader(
                                        title = stringResource(R.string.home_pending_title),
                                        iconImage = Icons.Rounded.Book,
                                        badge = pluralStringResource(
                                            R.plurals.pending_to_start,
                                            state.pendingBooks.size,
                                            state.pendingBooks.size
                                        ),
                                        badgeContentColor = MaterialTheme.colorScheme.secondary,
                                        badgeOutlined = true,
                                        actionLabel = stringResource(R.string.see_list),
                                        onAction = onViewPending,
                                        actionChevron = true
                                    )
                                }
                                items(
                                    items = state.pendingBooks,
                                    key = { book -> "pending-${book.id}" },
                                    contentType = { "pending" }
                                ) { book ->
                                    PendingBookRow(
                                        book = book,
                                        onOpen = { openBook(book) },
                                        onRead = { onNavigateToReadBook(book.id) },
                                        onDetails = { onNavigateToBookDetail(book.id) },
                                        onDelete = { bookToDelete = book }
                                    )
                                }
                            }

                            item(key = "add-or-import", contentType = "actions") {
                                AddOrImportBooksRow(
                                    onAddClick = onNavigateToAddBook,
                                    onImportClick = {
                                        if (!isImporting) {
                                            documentPickerLauncher.launch(
                                                arrayOf("application/epub+zip")
                                            )
                                        }
                                    },
                                    importEnabled = !isImporting
                                )
                            }
                        }
                    }
                }
            }
            if (isImporting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

