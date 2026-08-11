package com.juguito.juguitoreader.ui.book.detail

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.juguito.juguitoreader.ui.book.components.BookStatusDropdown
import com.juguito.juguitoreader.ui.book.components.FolderMultiSelector
import com.juguito.juguitoreader.ui.book.components.GenreHybridSelector
import com.juguito.juguitoreader.ui.book.components.RatingNumberInput
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import com.juguito.juguitoreader.utils.FileUtils
import com.juguito.juguitoreader.utils.FileUtils.getFileNameFromUri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                viewModel.onEvent(BookDetailEvent.OnCoverUrlChanged(it.toString()))
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempCameraUri?.let {
                    viewModel.onEvent(BookDetailEvent.OnCoverUrlChanged(it.toString()))
                }
            }
        }
    )

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                viewModel.onEvent(BookDetailEvent.OnLocalFilePathChanged(it.toString()))
            }
        }
    )

    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.startDate ?: System.currentTimeMillis()
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.endDate ?: System.currentTimeMillis()
    )

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let {
                        viewModel.onEvent(BookDetailEvent.OnStartDateChanged(it))
                    }
                    showStartDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let {
                        viewModel.onEvent(BookDetailEvent.OnEndDateChanged(it))
                    }
                    showEndDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Seleccionar portada") },
            text = { Text("¿Cómo quieres añadir la imagen?") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("Galería")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    val uri = FileUtils.getTempImageUri(context)
                    tempCameraUri = uri
                    cameraLauncher.launch(uri)
                }) {
                    Text("Cámara")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditMode) "Editar Libro" else state.bookDraft.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = LoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (state.isEditMode) {
                        IconButton(onClick = { viewModel.onEvent(BookDetailEvent.OnEditModeChanged(false)) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancelar")
                        }
                        IconButton(onClick = { viewModel.onEvent(BookDetailEvent.OnSaveClick) }) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Guardar")
                        }
                    } else {
                        IconButton(onClick = { viewModel.onEvent(BookDetailEvent.OnEditModeChanged(true)) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                PrimaryTabRow(selectedTabIndex = state.selectedTab) {
                    Tab(
                        selected = state.selectedTab == 0,
                        onClick = { viewModel.onEvent(BookDetailEvent.OnTabChanged(0)) },
                        text = { Text("Información") }
                    )
                    Tab(
                        selected = state.selectedTab == 1,
                        onClick = { viewModel.onEvent(BookDetailEvent.OnTabChanged(1)) },
                        text = { Text("Registro") }
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (state.selectedTab == 0) {
                        BookInfoTab(
                            state = state,
                            onEvent = viewModel::onEvent,
                            onPickCover = { showImageSourceDialog = true },
                            onPickFile = {
                                documentPickerLauncher.launch(arrayOf("application/epub+zip", "application/pdf"))
                            }
                        )
                    } else {
                        RegistryTab(
                            state = state,
                            onEvent = viewModel::onEvent,
                            onShowStartDatePicker = { showStartDatePicker = true },
                            onShowEndDatePicker = { showEndDatePicker = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookInfoTab(
    state: BookDetailUiState,
    onEvent: (BookDetailEvent) -> Unit,
    onPickCover: () -> Unit,
    onPickFile: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val draft = state.bookDraft

        if (state.isEditMode) {
            Text(text = "Ficha Técnica", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            // Edición de Portada y Archivo
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(100.dp, 150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onPickCover() },
                    contentAlignment = Alignment.Center
                ) {
                    if (draft.coverUrl != null) {
                        AsyncImage(model = draft.coverUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White)
                    }
                }
                
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onPickFile, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.FileUpload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Cambiar")
                        }
                        if (draft.localFilePath != null) {
                            IconButton(onClick = { onEvent(BookDetailEvent.OnLocalFilePathChanged(null)) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Quitar archivo", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    val fileName = getFileNameFromUri(context, draft.localFilePath)
                    Text(text = fileName, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
            }

            OutlinedTextField(
                value = draft.title,
                onValueChange = { onEvent(BookDetailEvent.OnTitleChanged(it)) },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = draft.author,
                onValueChange = { onEvent(BookDetailEvent.OnAuthorChanged(it)) },
                label = { Text("Autor") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = draft.publisher,
                onValueChange = { onEvent(BookDetailEvent.OnPublisherChanged(it)) },
                label = { Text("Editorial") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Row (
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ){
                OutlinedTextField(
                    value = state.bookDraft.series,
                    onValueChange = { onEvent(BookDetailEvent.OnSeriesChanged(it)) },
                    label = { Text("Saga del libro") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField( // Se puede limitar a 4 digitos como mucho?
                    value = state.bookDraft.seriesOrder,
                    onValueChange = { onEvent(BookDetailEvent.OnSeriesOrderChanged(it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    label = { Text("#") },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            GenreHybridSelector(
                availableGenres = state.availableGenres,
                selectedGenres = draft.genres,
                onGenresChanged = { onEvent(BookDetailEvent.OnGenresChanged(it)) }
            )

            FolderMultiSelector(
                availableFolders = state.availableFolders,
                selectedFolders = draft.folders,
                onFoldersChanged = { onEvent(BookDetailEvent.OnFoldersChanged(it)) }
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = draft.isPhysical,
                        onCheckedChange = { onEvent(BookDetailEvent.OnIsPhysicalChanged(it)) }
                    )
                    Text(text = "Libro en formato físico", style = MaterialTheme.typography.bodyMedium)
                }
            }

        } else {
            // Modo Lectura
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (draft.coverUrl != null) {
                    AsyncImage(
                        model = draft.coverUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .width(120.dp)
                            .aspectRatio(0.7f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .width(120.dp)
                            .aspectRatio(0.7f),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.padding(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = draft.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(text = "de ${draft.author}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                    if (draft.series.isNotBlank()) {
                        Text(text = "Saga: ${draft.series}, Volumen ${draft.seriesOrder}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary )
                    }
                    if (draft.publisher.isNotBlank()) {
                        Text(text = draft.publisher, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    val formatText = if (draft.isPhysical) "Formato Físico" else "Ebook / Digital"
                    val formatIcon = if (draft.isPhysical) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.Devices
                    
                    AssistChip(
                        onClick = {},
                        label = { Text(formatText) },
                        leadingIcon = { Icon(formatIcon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (draft.genres.isNotEmpty()) {
                Text(text = "Géneros", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    draft.genres.forEach { genre ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(genre.name) }
                        )
                    }
                }
            }

            if (draft.folders.isNotEmpty()) {
                Text(text = "Carpetas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    draft.folders.forEach { folder ->
                        AssistChip(
                            onClick = {},
                            label = { Text(folder.name) },
                            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }
            
            if (!draft.localFilePath.isNullOrBlank() && !draft.isPhysical) {
                Text(text = "Archivo local", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                val fileName = getFileNameFromUri(context, draft.localFilePath)
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RegistryTab(
    state: BookDetailUiState,
    onEvent: (BookDetailEvent) -> Unit,
    onShowStartDatePicker: () -> Unit,
    onShowEndDatePicker: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = "Progreso de lectura", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        BookStatusDropdown(
            selectedStatus = state.status,
            onStatusSelected = { onEvent(BookDetailEvent.OnStatusChanged(it)) }
        )

        RatingNumberInput(
            rating = state.rating ?: 0f,
            onRatingChanged = { onEvent(BookDetailEvent.OnRatingChanged(it)) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DatePickerField(
                label = "Fecha Inicio",
                dateMillis = state.startDate,
                onClick = onShowStartDatePicker,
                modifier = Modifier.weight(1f)
            )
            DatePickerField(
                label = "Fecha Fin",
                dateMillis = state.endDate,
                onClick = onShowEndDatePicker,
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(text = "Mis Notas y Reflexiones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = state.comment ?: "",
            onValueChange = { onEvent(BookDetailEvent.OnCommentChanged(it)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
            placeholder = { Text("Escribe aquí lo que piensas del libro, citas favoritas, dudas...") },
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = { onEvent(BookDetailEvent.OnSaveClick) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Guardar cambios de registro")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    dateMillis: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val dateText = remember(dateMillis) {
        if (dateMillis != null) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.format(Date(dateMillis))
        } else {
            "Seleccionar"
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = dateText,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onClick)
            )
        }
    }
}
