package com.juguito.juguitoreader.ui.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.ui.common.ObserveAsEvents
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.components.JuguitoDialog
import com.juguito.juguitoreader.ui.genre.AddGenreDialog
import com.juguito.juguitoreader.ui.theme.LoraFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementScreen(
    managementMessage: String? = null,
    onClearManagementMessage: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEditFolder: (Int) -> Unit,
    onNavigateToAddFolder: () -> Unit,
    viewModel: ManagementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    ManagementContent(
        state = state,
        onEvent = viewModel::onEvent,
        managementMessage = managementMessage,
        onClearManagementMessage = onClearManagementMessage,
        onNavigateBack = onNavigateBack,
        onNavigateToEditFolder = onNavigateToEditFolder,
        onNavigateToAddFolder = onNavigateToAddFolder,
        effect = viewModel.effect
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementContent(
    state: ManagementUiState,
    onEvent: (ManagementEvent) -> Unit,
    managementMessage: String?,
    onClearManagementMessage: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEditFolder: (Int) -> Unit,
    onNavigateToAddFolder: () -> Unit,
    effect: kotlinx.coroutines.flow.Flow<UiEffect>
) {
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(managementMessage) {
        managementMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearManagementMessage()
        }
    }

    var folderToDelete by remember { mutableStateOf<Folder?>(null) }
    var genreToDelete by remember { mutableStateOf<Genre?>(null) }
    var showAddGenreDialog by remember { mutableStateOf(false) }

    ObserveAsEvents(effect) { uiEffect ->
        when (uiEffect) {
            is UiEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(uiEffect.message)
            }
            else -> Unit
        }
    }

    LaunchedEffect(state.isSearchActive) {
        if (state.isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    if (folderToDelete != null) {
        JuguitoDialog(
            onDismissRequest = { folderToDelete = null },
            title = "Eliminar carpeta",
            message = "¿Estás seguro de que quieres eliminar la carpeta '${folderToDelete?.name}'? Los libros no se borrarán, solo se quitarán de esta carpeta.\n Esta acción no se puede deshacer.",
            confirmButtonText = "Eliminar",
            onConfirm = {
                folderToDelete?.let { onEvent(ManagementEvent.OnDeleteFolder(it.id)) }
                folderToDelete = null
            },
            dismissButtonText = "Cancelar",
            isDestructive = true
        )
    }

    if (genreToDelete != null) {
        JuguitoDialog(
            onDismissRequest = { genreToDelete = null },
            title = "Eliminar género",
            message = "¿Estás seguro de que quieres eliminar el género '${genreToDelete?.name}'? Los libros con este género dejaran de tenerlo asignado.\n Esta acción no se puede deshacer.",
            confirmButtonText = "Eliminar",
            onConfirm = {
                genreToDelete?.let { onEvent(ManagementEvent.OnDeleteGenre(it.id)) }
                genreToDelete = null
            },
            dismissButtonText = "Cancelar",
            isDestructive = true
        )
    }

    if (showAddGenreDialog) {
        AddGenreDialog(
            onDismissRequest = { showAddGenreDialog = false }
        )
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
                                onValueChange = { onEvent(ManagementEvent.OnSearchQueryChanged(it)) },
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
                            { onEvent(ManagementEvent.OnToggleSearch) }
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
                            IconButton(onClick = { onEvent(ManagementEvent.OnSearchQueryChanged("")) }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Color.White)
                            }
                        } else {
                            IconButton(onClick = { onEvent(ManagementEvent.OnToggleSearch) }) {
                                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White)
                            }
                        }
                        IconButton(onClick = { if (state.selectedTab == 0) {onNavigateToAddFolder()} else {showAddGenreDialog = true} }) {
                            Icon(Icons.Default.Add, contentDescription = "Crear", tint = Color.White)
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
            PrimaryTabRow(selectedTabIndex = state.selectedTab) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { onEvent(ManagementEvent.OnTabSelected(0)) },
                    text = { Text("Carpetas") }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { onEvent(ManagementEvent.OnTabSelected(1)) },
                    text = { Text("Géneros") }
                )
            }

            when (state.selectedTab) {
                0 -> FolderList(
                    folders = filteredFolders,
                    onEdit = { onNavigateToEditFolder(it.id) },
                    onDelete = { folderToDelete = it }
                )
                1 -> GenreList(
                    genres = filteredGenres,
                    genreToEditId = state.genreToEdit?.id,
                    editName = state.newGenreName,
                    onGenreNameChanged = { onEvent(ManagementEvent.OnGenreNameChanged(it)) },
                    onUpdateConfirm = { onEvent(ManagementEvent.OnUpdateGenreConfirm) },
                    onCancelEdit = { onEvent(ManagementEvent.OnCancelEditGenre) },
                    onEdit = { onEvent(ManagementEvent.OnEditGenreClick(it)) },
                    onDelete = { genreToDelete = it }
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
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { onEdit(folder) }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(folder.colorHex.toColorInt()).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color(folder.colorHex.toColorInt()),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (folder.bookCount == 1) "• 1 libro" else "• ${folder.bookCount} libros",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = folder.description ?: "Sin descripción",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Label,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = genre.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (genre.bookCount == 1) "1 libro" else "${genre.bookCount} libros",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
