package com.juguito.juguitoreader.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.ui.theme.LoraFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToReadBook: (Int) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var isSearchVisible by remember { mutableStateOf(false) }
    var showAllFoldersSheet by remember { mutableStateOf(false) }

    if (showAllFoldersSheet) {
        AllFoldersSheet(
            folders = state.folders,
            selectedFolder = state.selectedFolder,
            onFolderSelected = { 
                viewModel.onEvent(LibraryEvent.OnSelectedFolderChanged(it))
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
                            value = state.searchText,
                            onValueChange = { viewModel.onEvent(LibraryEvent.OnSearchTextChanged(it)) },
                            placeholder = { Text("Buscar en mi biblioteca...", fontSize = 14.sp, color = Color.White) },
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
                        Text(
                            text = state.selectedFolder?.name ?: "Todos los libros",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = if (isSearchVisible) { { isSearchVisible = false; viewModel.onEvent(LibraryEvent.OnSearchTextChanged("")) } } else onOpenDrawer) {
                        Icon(
                            imageVector = if (isSearchVisible) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    if (!isSearchVisible) {
                        IconButton(onClick = { isSearchVisible = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Selector de Carpetas con botón de expansión
            FolderSelectorRow(
                folders = state.folders,
                selectedFolder = state.selectedFolder,
                onFolderSelected = { viewModel.onEvent(LibraryEvent.OnSelectedFolderChanged(it)) },
                onExpandClick = { showAllFoldersSheet = true }
            )

            // 2. Grid de Libros
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.filteredBooks.isEmpty()) {
                    EmptyLibraryState(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.filteredBooks) { book ->
                            BookGridItem(
                                book = book,
                                onClick = { onNavigateToReadBook(book.id) }
                            )
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
            items(folders) { folder ->
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

@Composable
fun BookGridItem(book: Book, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
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
            
            // Badge de Estado superpuesto (Banner inferior)
            StatusBadge(
                status = book.status,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            )
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
        BookStatus.PENDING -> return // No badge for pending
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
fun EmptyLibraryState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No hay libros en esta carpeta",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
