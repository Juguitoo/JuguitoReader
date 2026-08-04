package com.juguito.juguitoreader.ui.book.add

import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import androidx.core.net.toUri
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.ui.book.components.BookStatusDropdown
import com.juguito.juguitoreader.ui.book.components.FolderMultiSelector
import com.juguito.juguitoreader.ui.book.components.GenreHybridSelector
import com.juguito.juguitoreader.ui.book.components.RatingNumberInput
import com.juguito.juguitoreader.utils.EpubParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddBookViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showImportDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                viewModel.onEvent(AddBookEvent.OnCoverUrlChanged(it.toString()))
            }
        }
    )

    val basicDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                viewModel.onEvent(AddBookEvent.OnLocalFilePathChanged(it.toString()))

                scope.launch(Dispatchers.IO) {
                    val metadata = EpubParser.extractMetadata(context, it)

                    withContext(Dispatchers.Main) {
                        if (!metadata.coverUrl.isNullOrBlank()) {
                            viewModel.onEvent(AddBookEvent.OnCoverUrlChanged(metadata.coverUrl))
                        }
                    }
                }
            }
        }
    )

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                viewModel.onEvent(AddBookEvent.OnLocalFilePathChanged(it.toString()))

                scope.launch(Dispatchers.IO) {
                    val metadata = EpubParser.extractMetadata(context, it)

                    withContext(Dispatchers.Main) {
                        if (!metadata.title.isNullOrBlank()) {
                            viewModel.onEvent(AddBookEvent.OnTitleChanged(metadata.title))
                        }
                        if (!metadata.author.isNullOrBlank()) {
                            viewModel.onEvent(AddBookEvent.OnAuthorChanged(metadata.author))
                        }
                        if (!metadata.publisher.isNullOrBlank()) {
                            viewModel.onEvent(AddBookEvent.OnPublisherChanged(metadata.publisher))
                        }
                        if (metadata.genres.isNotEmpty()) {
                            val newGenres = state.genres + metadata.genres.map { genreName ->
                                Genre(
                                    name = genreName
                                )
                            }
                            val uniqueGenres = newGenres.distinctBy { it.name.lowercase() }
                            viewModel.onEvent(AddBookEvent.OnGenresChanged(uniqueGenres))
                        }
                        if (!metadata.coverUrl.isNullOrBlank()) {
                            viewModel.onEvent(AddBookEvent.OnCoverUrlChanged(metadata.coverUrl))
                        }
                    }
                }
            }
        }
    )

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            icon = { Icon(Icons.Default.Download, contentDescription = null) },
            title = { Text(text = "Importar libro") },
            text = {
                Text(text = "¿Deseas seleccionar un archivo EPUB para extraer sus datos automáticamente? Esto sobrescribirá el título, autor y cualquier otro dato que ya hayas rellenado.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportDialog = false
                        documentPickerLauncher.launch(arrayOf("application/epub+zip", "application/pdf"))
                    }
                ) {
                    Text("Importar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 6.dp,
                modifier = Modifier.background(Color.Transparent).statusBarsPadding()
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            "Nuevo Libro",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Atrás",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = {showImportDialog = true}) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Autocompletar con EPUB",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Detalles del libro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onEvent(AddBookEvent.OnTitleChanged(it)) },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.errorMessage != null && state.title.isBlank(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = state.author,
                onValueChange = { viewModel.onEvent(AddBookEvent.OnAuthorChanged(it)) },
                label = { Text("Autor") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = state.publisher,
                onValueChange = { viewModel.onEvent(AddBookEvent.OnPublisherChanged(it)) },
                label = { Text("Editorial") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(0.6f)) {
                    BookStatusDropdown(
                        selectedStatus = state.status,
                        onStatusSelected = { viewModel.onEvent(AddBookEvent.OnStatusChanged(it)) }
                    )
                }
                Box(modifier = Modifier.weight(0.4f)) {
                    RatingNumberInput(
                        rating = state.rating,
                        onRatingChanged = { viewModel.onEvent(AddBookEvent.OnRatingChanged(it)) }
                    )
                }
            }

            GenreHybridSelector(
                availableGenres = state.availableGenres,
                selectedGenres = state.genres,
                onGenresChanged = { viewModel.onEvent(AddBookEvent.OnGenresChanged(it)) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = "Organización",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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
                        checked = state.isPhysical,
                        onCheckedChange = { viewModel.onEvent(AddBookEvent.OnIsPhysicalChanged(it)) }
                    )
                    Text(
                        text = "Libro en formato físico",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            FolderMultiSelector(
                availableFolders = state.availableFolders,
                selectedFolders = state.folders,
                onFoldersChanged = { viewModel.onEvent(AddBookEvent.OnFoldersChanged(it)) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = "Archivos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min), // Permite que los hijos usen fillMaxHeight
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Portada a la izquierda (Altura completa)
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .fillMaxHeight()
                ) {
                    if (!state.coverUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = state.coverUrl,
                            contentDescription = "Portada seleccionada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.padding(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Selector de Archivo
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            
                            Text(
                                text = "Archivo de lectura",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val fileName = getFileNameFromUri(context, state.localFilePath)
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    basicDocumentLauncher.launch(arrayOf("application/epub+zip", "application/pdf"))
                                },
                                modifier = Modifier.height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = if (state.localFilePath == null) "Seleccionar" else "Cambiar",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (state.coverUrl == null) "Añadir portada" else "Cambiar portada",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onEvent(AddBookEvent.OnSaveClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Guardar Libro", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

private fun getFileNameFromUri(context: android.content.Context, uriString: String?): String {
    if (uriString.isNullOrBlank()) return "Ningún archivo seleccionado"

    val uri = uriString.toUri()
    var result: String? = null

    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = it.getString(index)
                }
            }
        }
    }

    if (result == null) {
        result = uri.path?.substringAfterLast('/')
    }

    return result ?: "Archivo desconocido"
}
