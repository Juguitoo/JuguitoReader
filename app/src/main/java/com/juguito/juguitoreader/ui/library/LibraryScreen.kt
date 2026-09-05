package com.juguito.juguitoreader.ui.library

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.common.ObserveAsEvents
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.common.components.EmptyLibraryView
import com.juguito.juguitoreader.ui.components.ErrorView
import com.juguito.juguitoreader.ui.library.LibraryEvent.*
import com.juguito.juguitoreader.ui.library.components.AllFoldersSheet
import com.juguito.juguitoreader.ui.library.components.BookGridItem
import com.juguito.juguitoreader.ui.library.components.EmptySearchPlaceholder
import com.juguito.juguitoreader.ui.library.components.FolderSelectorRow
import com.juguito.juguitoreader.ui.theme.LoraFontFamily

@OptIn(ExperimentalMaterial3Api::class)
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

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
                        IconButton(onClick = { onToggleSearch(true) }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_hint))
                        }
                        IconButton(
                            onClick = {
                                documentPickerLauncher.launch(arrayOf("application/epub+zip"))
                            },
                            enabled = !isImporting
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = stringResource(R.string.import_book_title),
                                tint = Color.White
                            )
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

                        Box(modifier = Modifier.fillMaxSize()) {
                            if (state.filteredBooks.isEmpty()) {
                                EmptySearchPlaceholder(
                                    isSearching = state.searchText.isNotBlank(),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyVerticalGrid(
                                    columns = Adaptive(minSize = 90.dp),
                                    contentPadding = PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(24.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(state.filteredBooks, key = { it.id }) { book ->
                                        BookGridItem(
                                            book = book,
                                            onClick = {
                                                val canOpenReader = !book.isPhysical && !book.localFilePath.isNullOrBlank()
                                                if (canOpenReader) onNavigateToReadBook(book.id)
                                                else onNavigateToBookDetail(book.id)
                                            },
                                            onDetailClick = { onNavigateToBookDetail(book.id) },
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

