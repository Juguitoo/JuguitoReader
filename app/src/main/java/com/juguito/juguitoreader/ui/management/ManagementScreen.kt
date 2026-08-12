package com.juguito.juguitoreader.ui.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.ui.theme.LoraFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditFolder: (Int) -> Unit,
    viewModel: ManagementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.isSearchActive) {
        if (state.isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 6.dp,
                modifier = Modifier.statusBarsPadding()
            ) {
                TopAppBar(
                    title = {
                        if (state.isSearchActive) {
                            TextField(
                                value = state.searchQuery,
                                onValueChange = { viewModel.onEvent(ManagementEvent.OnSearchQueryChanged(it)) },
                                placeholder = { 
                                    Text(
                                        "Buscar...", 
                                        color = Color.White.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodyLarge
                                    ) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    cursorColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        } else {
                            Text(
                                "Gestor de contenido",
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = LoraFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = if (state.isSearchActive) {
                            { viewModel.onEvent(ManagementEvent.OnToggleSearch) }
                        } else onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (state.isSearchActive) {
                            IconButton(onClick = { viewModel.onEvent(ManagementEvent.OnSearchQueryChanged("")) }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Color.White)
                            }
                        } else {
                            IconButton(onClick = { viewModel.onEvent(ManagementEvent.OnToggleSearch) }) {
                                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White
                    )
                )
            }
        }
    ) { paddingValues ->
        val filteredFolders = remember(state.folders, state.searchQuery) {
            state.folders.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
        }
        val filteredGenres = remember(state.genres, state.searchQuery) {
            state.genres.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = state.selectedTab) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.onEvent(ManagementEvent.OnTabSelected(0)) },
                    text = { Text("Carpetas") }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.onEvent(ManagementEvent.OnTabSelected(1)) },
                    text = { Text("Géneros") }
                )
            }

            when (state.selectedTab) {
                0 -> FolderList(
                    folders = filteredFolders,
                    onEdit = { onNavigateToEditFolder(it.id) },
                    onDelete = { viewModel.onEvent(ManagementEvent.OnDeleteFolder(it.id)) }
                )
                1 -> GenreList(
                    genres = filteredGenres,
                    genreToEditId = state.genreToEdit?.id,
                    editName = state.newGenreName,
                    onGenreNameChanged = { viewModel.onEvent(ManagementEvent.OnGenreNameChanged(it)) },
                    onUpdateConfirm = { viewModel.onEvent(ManagementEvent.OnUpdateGenreConfirm) },
                    onCancelEdit = { viewModel.onEvent(ManagementEvent.OnCancelEditGenre) },
                    onEdit = { viewModel.onEvent(ManagementEvent.OnEditGenreClick(it)) },
                    onDelete = { viewModel.onEvent(ManagementEvent.OnDeleteGenre(it.id)) }
                )
            }
        }
    }
}

@Composable
fun FolderList(
    folders: List<Folder>,
    onEdit: (Folder) -> Unit,
    onDelete: (Folder) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(folders) { folder ->
            FolderItem(folder, onEdit, onDelete)
        }
    }
}

@Composable
fun FolderItem(
    folder: Folder,
    onEdit: (Folder) -> Unit,
    onDelete: (Folder) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(folder.colorHex.toColorInt()))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!folder.description.isNullOrBlank()) {
                    Text(
                        text = folder.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = { onEdit(folder) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { onDelete(folder) }) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun GenreList(
    genres: List<Genre>,
    genreToEditId: Int?,
    editName: String,
    onGenreNameChanged: (String) -> Unit,
    onUpdateConfirm: () -> Unit,
    onCancelEdit: () -> Unit,
    onEdit: (Genre) -> Unit,
    onDelete: (Genre) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(genres, key = { it.id }) { genre ->
            GenreItem(
                genre = genre,
                isEditing = genre.id == genreToEditId,
                editName = editName,
                onNameChange = onGenreNameChanged,
                onConfirm = onUpdateConfirm,
                onCancel = onCancelEdit,
                onEdit = { onEdit(genre) },
                onDelete = { onDelete(genre) }
            )
        }
    }
}

@Composable
fun GenreItem(
    genre: Genre,
    isEditing: Boolean,
    editName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var textFieldValue by remember { mutableStateOf(TextFieldValue(editName)) }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            textFieldValue = TextFieldValue(
                text = editName,
                selection = TextRange(editName.length)
            )
            focusRequester.requestFocus()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditing) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        onNameChange(it.text)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        onConfirm()
                        focusManager.clearFocus()
                    })
                )
                IconButton(onClick = onConfirm) {
                    Icon(Icons.Default.Check, contentDescription = "Guardar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.error)
                }
            } else {
                Text(
                    text = genre.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
