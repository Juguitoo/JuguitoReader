package com.juguito.juguitoreader.ui.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.ui.common.ObserveAsEvents
import com.juguito.juguitoreader.ui.common.components.EmptyLibraryView
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.components.ErrorView
import com.juguito.juguitoreader.ui.home.components.PendingBookRow
import com.juguito.juguitoreader.ui.library.LibraryEvent.OnFavouritesFilterChanged
import com.juguito.juguitoreader.ui.library.LibraryEvent.OnSearchTextChanged
import com.juguito.juguitoreader.ui.library.LibraryEvent.OnSelectedFolderChanged
import com.juguito.juguitoreader.ui.library.LibraryEvent.OnStatusChanged
import com.juguito.juguitoreader.ui.library.LibraryEvent.OnStatusFilterChanged
import com.juguito.juguitoreader.ui.library.components.AllFoldersSheet
import com.juguito.juguitoreader.ui.library.components.BookLibraryGridItem
import com.juguito.juguitoreader.ui.library.components.BookLibraryRow
import com.juguito.juguitoreader.ui.library.components.EmptySearchPlaceholder
import com.juguito.juguitoreader.ui.library.components.FolderSelectorRow
import com.juguito.juguitoreader.ui.theme.LoraFontFamily

@Composable
fun LibraryScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToAddBook: () -> Unit,
    onNavigateToReadBook: (Int) -> Unit,
    onNavigateToBookDetail: (Int) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    var isSearchVisible by remember { mutableStateOf(false) }
    var showAllFoldersSheet by remember { mutableStateOf(false) }

    LibraryContent(
        state = state,
        isImporting = isImporting,
        isSearchVisible = isSearchVisible,
        showAllFoldersSheet = showAllFoldersSheet,
        onToggleSearch = { isSearchVisible = it },
        onShowAllFoldersSheet = { showAllFoldersSheet = it },
        onOpenDrawer = onOpenDrawer,
        onNavigateToAddBook = onNavigateToAddBook,
        onNavigateToReadBook = onNavigateToReadBook,
        onNavigateToBookDetail = onNavigateToBookDetail,
        onEvent = viewModel::onEvent,
        onImportBook = viewModel::importBook,
        onDismissError = viewModel::dismissError,
        effect = viewModel.effect
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryContent(
    state: LibraryUiState,
    isImporting: Boolean,
    isSearchVisible: Boolean,
    showAllFoldersSheet: Boolean,
    onToggleSearch: (Boolean) -> Unit,
    onShowAllFoldersSheet: (Boolean) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToAddBook: () -> Unit,
    onNavigateToReadBook: (Int) -> Unit,
    onNavigateToBookDetail: (Int) -> Unit,
    onEvent: (LibraryEvent) -> Unit,
    onImportBook: (Uri) -> Unit,
    onDismissError: () -> Unit,
    effect: kotlinx.coroutines.flow.Flow<UiEffect>
) {
    val context = LocalContext.current
    val currentSelectedFolder = when (state) {
        is LibraryUiState.Success -> state.selectedFolder
        is LibraryUiState.Empty -> state.selectedFolder
        else -> null
    }
    val currentSearchText = when (state) {
        is LibraryUiState.Success -> state.searchText
        is LibraryUiState.Empty -> state.searchText
        else -> ""
    }
    val currentFolders = when (state) {
        is LibraryUiState.Success -> state.folders
        is LibraryUiState.Empty -> state.folders
        else -> emptyList()
    }
    var showMenu by remember { mutableStateOf(false) }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                onImportBook(it)
            }
        }
    )

    if (showAllFoldersSheet && state is LibraryUiState.Success) {
        AllFoldersSheet(
            folders = currentFolders,
            selectedFolder = currentSelectedFolder,
            onFolderSelected = { folder ->
                onEvent(OnSelectedFolderChanged(folder))
                onShowAllFoldersSheet(false)
            },
            onDismiss = { onShowAllFoldersSheet(false) }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    ObserveAsEvents(effect) { uiEffect ->
        when (uiEffect) {
            is UiEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(uiEffect.message.asString(context))
            }
            else -> Unit
        }
    }

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
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    if (isSearchVisible) {
                        TextField(
                            value = currentSearchText,
                            onValueChange = { onEvent(OnSearchTextChanged(it)) },
                            placeholder = { Text(stringResource(R.string.search_library_hint), fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White
                            ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        val folderName = (state as? LibraryUiState.Success)?.selectedFolder?.name ?: stringResource(R.string.all_books)
                        Text(
                            text = folderName,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSearchVisible) {
                                onToggleSearch(false)
                                onEvent(OnSearchTextChanged(""))
                            } else {
                                onOpenDrawer()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSearchVisible) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
                            contentDescription = if (isSearchVisible) stringResource(R.string.return_text) else stringResource(R.string.menu)
                        )
                    }
                },
                actions = {
                    if (!isSearchVisible) {
                        val favouritesOnly = (state as? LibraryUiState.Success)?.favouritesOnly == true
                        if (state is LibraryUiState.Success) {
                            IconButton(
                                onClick = { onEvent(OnFavouritesFilterChanged(!favouritesOnly)) }
                            ) {
                                Icon(
                                    imageVector = if (favouritesOnly) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = stringResource(
                                        if (favouritesOnly) R.string.clear_favourites_filter else R.string.show_favourites_only
                                    ),
                                    tint = Color.White
                                )
                            }
                        }
                        IconButton(onClick = { onToggleSearch(true) }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_hint))
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.options))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.import_book_title)) },
                                onClick = {
                                    showMenu = false
                                    documentPickerLauncher.launch(arrayOf("application/epub+zip"))
                                },
                                enabled = !isImporting
                            )
                            if (state is LibraryUiState.Success) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(if (state.layoutMode == 0) R.string.library_rows else R.string.library_grid)) },
                                    onClick = {
                                        when (state.layoutMode) {
                                            0 -> onEvent(LibraryEvent.OnLayoutModeChanged(1))
                                            1 -> onEvent(LibraryEvent.OnLayoutModeChanged(0))
                                        }
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    } else {
                        val currentSearch = (state as? LibraryUiState.Success)?.searchText ?: ""
                        if (currentSearch.isNotEmpty()) {
                            IconButton(onClick = { onEvent(OnSearchTextChanged("")) }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_search))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is LibraryUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is LibraryUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { onDismissError() }
                    )
                }

                is LibraryUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        FolderSelectorRow(
                            folders = state.folders,
                            selectedFolder = state.selectedFolder,
                            onFolderSelected = { folder ->
                                onEvent(OnSelectedFolderChanged(folder))
                            },
                            onExpandClick = { onShowAllFoldersSheet(true) }
                        )
                        state.status?.let { status ->
                            Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)) {
                                FilterChip(
                                    selected = true,
                                    onClick = { onEvent(OnStatusFilterChanged(null)) },
                                    label = {
                                        Text(
                                            text = stringResource(
                                                when (status) {
                                                    BookStatus.PENDING -> R.string.status_pending
                                                    BookStatus.READING -> R.string.status_reading
                                                    BookStatus.FINISHED -> R.string.status_finished
                                                    BookStatus.DROPPED -> R.string.status_dropped
                                                }
                                            )
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.clear_status_filter)
                                        )
                                    }
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            if (state.filteredBooks.isEmpty()) {
                                EmptySearchPlaceholder(
                                    isSearching = state.searchText.isNotBlank(),
                                    isFilteringFavourites = state.favouritesOnly,
                                    isFilteringStatus = state.status != null,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                HorizontalDivider()
                                val isRowLayout = state.layoutMode == 1
                                LazyColumn(
                                    contentPadding = PaddingValues(
                                        start = 16.dp,
                                        top = if (isRowLayout) 0.dp else 16.dp,
                                        end = 16.dp,
                                        bottom = 16.dp
                                    ),
                                    verticalArrangement = if (isRowLayout) {
                                        Arrangement.Top
                                    } else {
                                        Arrangement.spacedBy(24.dp)
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = if (isRowLayout) 0.dp else 8.dp)
                                ) {
                                    when (state.layoutMode) {
                                        0 -> {
                                            val rows = state.filteredBooks.chunked(3)
                                            rows.forEachIndexed { index, rowBooks ->
                                                item(
                                                    key = rowBooks.first().id,
                                                    contentType = "row"
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            16.dp
                                                        )
                                                    ) {
                                                        rowBooks.forEach { book ->
                                                            Box(Modifier.weight(1f)) {
                                                                BookLibraryGridItem(
                                                                    book = book,
                                                                    onClick = {
                                                                        val canOpenReader =
                                                                            !book.isPhysical && !book.localFilePath.isNullOrBlank()
                                                                        if (canOpenReader) onNavigateToReadBook(
                                                                            book.id
                                                                        )
                                                                        else onNavigateToBookDetail(
                                                                            book.id
                                                                        )
                                                                    },
                                                                    onDetailClick = {
                                                                        onNavigateToBookDetail(
                                                                            book.id
                                                                        )
                                                                    },
                                                                    onStatusChange = { newStatus ->
                                                                        onEvent(
                                                                            OnStatusChanged(
                                                                                book.id,
                                                                                newStatus
                                                                            )
                                                                        )
                                                                    }
                                                                )
                                                            }
                                                        }
                                                        repeat(3 - rowBooks.size) {
                                                            Spacer(Modifier.weight(1f))
                                                        }
                                                    }

                                                }
                                                if (index < rows.lastIndex) {
                                                    item(
                                                        key = "divider-${rowBooks.last().id}",
                                                        contentType = "divider"
                                                    ) {
                                                        HorizontalDivider()
                                                    }
                                                }
                                            }
                                        }

                                        1 -> {
                                            state.filteredBooks.forEach { book ->
                                                val openBook: (Book) -> Unit = { book ->
                                                    val canRead = !book.isPhysical && !book.localFilePath.isNullOrBlank()
                                                    if (canRead) onNavigateToReadBook(book.id) else onNavigateToBookDetail(book.id)
                                                }
                                                item(key = book.id, contentType = "book") {
                                                    BookLibraryRow(
                                                        book = book,
                                                        onOpen = { openBook(book) },
                                                        onDetails = { onNavigateToBookDetail(book.id) },
                                                        onStatusChange = { newStatus ->
                                                            onEvent(
                                                                OnStatusChanged(
                                                                    book.id,
                                                                    newStatus
                                                                )
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                is LibraryUiState.Empty -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize()) {
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
                                modifier = Modifier.align(Alignment.Center)
                            )
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

