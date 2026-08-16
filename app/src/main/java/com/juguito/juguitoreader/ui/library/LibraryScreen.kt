package com.juguito.juguitoreader.ui.library

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.juguito.juguitoreader.ui.components.EmptyLibraryView
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
    val context = LocalContext.current
    var isSearchVisible by remember { mutableStateOf(false) }
    var showAllFoldersSheet by remember { mutableStateOf(false) }

    val currentSelectedFolder = when (val s = state) {
        is LibraryUiState.Success -> s.selectedFolder
        is LibraryUiState.Empty -> s.selectedFolder
        else -> null
    }
    val currentSearchText = when (val s = state) {
        is LibraryUiState.Success -> s.searchText
        is LibraryUiState.Empty -> s.searchText
        else -> ""
    }
    val currentFolders = when (val s = state) {
        is LibraryUiState.Success -> s.folders
        is LibraryUiState.Empty -> s.folders
        else -> emptyList()
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                viewModel.importBook(it)
            }
        }
    )

    if (showAllFoldersSheet && state is LibraryUiState.Success) {
        AllFoldersSheet(
            folders = currentFolders,
            selectedFolder = currentSelectedFolder,
            onFolderSelected = { folder ->
                viewModel.onEvent(OnSelectedFolderChanged(folder))
                showAllFoldersSheet = false
            },
            onDismiss = { showAllFoldersSheet = false }
        )
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    if (isSearchVisible) {
                        TextField(
                            value = currentSearchText,
                            onValueChange = { viewModel.onEvent(OnSearchTextChanged(it)) },
                            placeholder = { Text("Buscar en mi biblioteca...", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f)) },
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
                        val folderName = (state as? LibraryUiState.Success)?.selectedFolder?.name ?: "Todos los libros"
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
                                isSearchVisible = false
                                viewModel.onEvent(OnSearchTextChanged(""))
                            } else {
                                onOpenDrawer()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSearchVisible) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
                            contentDescription = if (isSearchVisible) "Volver" else "Menú"
                        )
                    }
                },
                actions = {
                    if (!isSearchVisible) {
                        IconButton(onClick = { isSearchVisible = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                        IconButton(onClick = {
                            documentPickerLauncher.launch(arrayOf("application/epub+zip", "application/pdf"))
                        }) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "Importar libro",
                                tint = Color.White
                            )
                        }
                    } else {
                        val currentSearch = (state as? LibraryUiState.Success)?.searchText ?: ""
                        if (currentSearch.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onEvent(OnSearchTextChanged("")) }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
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
            when (val uiState = state) {
                is LibraryUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is LibraryUiState.Error -> {
                    ErrorView(
                        message = uiState.message,
                        onRetry = { viewModel.dismissError() }
                    )
                }

                is LibraryUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        FolderSelectorRow(
                            folders = uiState.folders,
                            selectedFolder = uiState.selectedFolder,
                            onFolderSelected = { folder ->
                                viewModel.onEvent(OnSelectedFolderChanged(folder))
                            },
                            onExpandClick = { showAllFoldersSheet = true }
                        )

                        Box(modifier = Modifier.fillMaxSize()) {
                            if (uiState.filteredBooks.isEmpty()) {
                                EmptySearchPlaceholder(
                                    isSearching = uiState.searchText.isNotBlank(),
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
                                    items(uiState.filteredBooks, key = { it.id }) { book ->
                                        BookGridItem(
                                            book = book,
                                            onClick = { onNavigateToReadBook(book.id) },
                                            onDetailClick = { onNavigateToBookDetail(book.id) },
                                            onStatusChange = { newStatus ->
                                                viewModel.onEvent(
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
                        FolderSelectorRow(
                            folders = uiState.folders,
                            selectedFolder = uiState.selectedFolder,
                            onFolderSelected = { folder ->
                                viewModel.onEvent(OnSelectedFolderChanged(folder))
                            },
                            onExpandClick = { showAllFoldersSheet = true }
                        )

                        Box(modifier = Modifier.fillMaxSize()) {
                            EmptyLibraryView(
                                onNavigateToAddBook = onNavigateToAddBook,
                                onImportClick = {
                                    documentPickerLauncher.launch(arrayOf("application/epub+zip", "application/pdf"))
                                },
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}

