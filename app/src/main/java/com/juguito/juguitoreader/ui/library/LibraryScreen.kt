package com.juguito.juguitoreader.ui.library

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.ui.theme.LoraFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToReadBook: (Int) -> Unit,
    onNavigateToBookDetail: (Int) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isSearchVisible by remember { mutableStateOf(false) }
    var showAllFoldersSheet by remember { mutableStateOf(false) }

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
        val successState = state as LibraryUiState.Success
        AllFoldersSheet(
            folders = successState.folders,
            selectedFolder = successState.selectedFolder,
            onFolderSelected = { folder ->
                viewModel.onEvent(LibraryEvent.OnSelectedFolderChanged(folder))
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
                        val currentSearch = (state as? LibraryUiState.Success)?.searchText ?: ""
                        TextField(
                            value = currentSearch,
                            onValueChange = { viewModel.onEvent(LibraryEvent.OnSearchTextChanged(it)) },
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
                                viewModel.onEvent(LibraryEvent.OnSearchTextChanged(""))
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
                            IconButton(onClick = { viewModel.onEvent(LibraryEvent.OnSearchTextChanged("")) }) {
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Algo ha salido mal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.dismissError() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }

                is LibraryUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        FolderSelectorRow(
                            folders = uiState.folders,
                            selectedFolder = uiState.selectedFolder,
                            onFolderSelected = { folder ->
                                viewModel.onEvent(LibraryEvent.OnSelectedFolderChanged(folder))
                            },
                            onExpandClick = { showAllFoldersSheet = true }
                        )

                        Box(modifier = Modifier.fillMaxSize()) {
                            if (uiState.filteredBooks.isEmpty()) {
                                EmptyLibraryState(
                                    isSearching = uiState.searchText.isNotBlank(),
                                    hasFolderSelected = uiState.selectedFolder != null,
                                    onImportClick = {
                                        documentPickerLauncher.launch(arrayOf("application/epub+zip", "application/pdf"))
                                    },
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 90.dp),
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
                                                viewModel.onEvent(LibraryEvent.OnStatusChanged(book.id, newStatus))
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSelectorRow(
    folders: List<Folder>,
    selectedFolder: Folder?,
    onFolderSelected: (Folder?) -> Unit,
    onExpandClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onExpandClick,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Ver todas las carpetas",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        VerticalDivider(modifier = Modifier.height(24.dp), thickness = 1.dp)

        LazyRow(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFolder == null,
                    onClick = { onFolderSelected(null) },
                    label = { Text("Todos") },
                    shape = CircleShape
                )
            }
            items(folders, key = { it.id }) { folder ->
                val isSelected = selectedFolder?.id == folder.id
                val color = Color(folder.colorHex.toColorInt())

                FilterChip(
                    selected = isSelected,
                    onClick = { onFolderSelected(folder) },
                    label = { Text(folder.name) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.15f),
                        selectedLabelColor = color,
                        selectedLeadingIconColor = color
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AllFoldersSheet(
    folders: List<Folder>,
    selectedFolder: Folder?,
    onFolderSelected: (Folder?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Mis Carpetas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFolder == null,
                    onClick = { onFolderSelected(null) },
                    label = { Text("Todos los libros") },
                    shape = RoundedCornerShape(12.dp)
                )

                folders.forEach { folder ->
                    val isSelected = selectedFolder?.id == folder.id
                    val color = Color(folder.colorHex.toColorInt())
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFolderSelected(folder) },
                        label = { Text(folder.name) },
                        leadingIcon = {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.15f),
                            selectedLabelColor = color
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookGridItem(
    book: Book,
    onClick: () -> Unit,
    onDetailClick: () -> Unit,
    onStatusChange: (BookStatus) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.7f)) {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (book.coverUrl != null) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            StatusBadge(
                status = book.status,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Detalles") },
                    onClick = {
                        showMenu = false
                        onDetailClick()
                    },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                )

                HorizontalDivider()

                Text(
                    "Cambiar estado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )

                BookStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status.displayName) },
                        onClick = {
                            showMenu = false
                            onStatusChange(status)
                        },
                        leadingIcon = {
                            if (book.status == status) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = book.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = book.author,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StatusBadge(status: BookStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        BookStatus.READING -> MaterialTheme.colorScheme.primary
        BookStatus.FINISHED -> Color(0xFF4CAF50)
        BookStatus.DROPPED -> MaterialTheme.colorScheme.error
        BookStatus.PENDING -> return
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp),
        color = color.copy(alpha = 0.85f),
        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = status.displayName.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun EmptyLibraryState(
    isSearching: Boolean,
    hasFolderSelected: Boolean,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when {
                isSearching -> "No se encontraron libros que coincidan"
                hasFolderSelected -> "No hay libros en esta carpeta"
                else -> "Tu biblioteca está vacía"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!isSearching && !hasFolderSelected) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onImportClick) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importar primer libro")
            }
        }
    }
}