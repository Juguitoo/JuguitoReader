package com.juguito.juguitoreader.ui.book.add

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.book.components.FolderMultiSelector
import com.juguito.juguitoreader.ui.book.components.GenreHybridSelector
import com.juguito.juguitoreader.ui.common.ObserveAsEvents
import com.juguito.juguitoreader.ui.common.components.DialogOptionCard
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.components.JuguitoDialog
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import com.juguito.juguitoreader.utils.FileUtils
import com.juguito.juguitoreader.utils.FileUtils.getFileNameFromUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(
    onNavigateBack: () -> Unit,
    onBookSavedSuccessfully: () -> Unit,
    onNavigateToAddFolder: () -> Unit,
    viewModel: AddBookViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    AddBookContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToAddFolder = onNavigateToAddFolder,
        onBookSavedSuccessfully = onBookSavedSuccessfully,
        effect = viewModel.effect
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookContent(
    state: AddBookUiState,
    onEvent: (AddBookEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToAddFolder: () -> Unit,
    onBookSavedSuccessfully: () -> Unit,
    effect: kotlinx.coroutines.flow.Flow<UiEffect>
) {
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                onEvent(AddBookEvent.OnCoverUrlChanged(it))
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempCameraUri?.let {
                    onEvent(AddBookEvent.OnCoverUrlChanged(it))
                }
            }
        }
    )

    val basicDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                onEvent(AddBookEvent.OnEpubFilePicked(it))
            }
        }
    )

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                onEvent(AddBookEvent.OnImportEpub(it))
            }
        }
    )

    if (showImportDialog) {
        JuguitoDialog(
            onDismissRequest = { showImportDialog = false },
            icon = Icons.Default.Download,
            title = stringResource(R.string.import_book_title),
            message = stringResource(R.string.import_epub_message),
            confirmButtonText = stringResource(R.string.import_text),
            onConfirm = {
                showImportDialog = false
                documentPickerLauncher.launch(arrayOf("application/epub+zip"))
            },
            dismissButtonText = stringResource(R.string.cancel),
        )
    }

    if (showImageSourceDialog) {
        JuguitoDialog(
            onDismissRequest = { showImageSourceDialog = false },
            icon = Icons.Default.AddPhotoAlternate,
            title = stringResource(R.string.select_cover),
            message = stringResource(R.string.image_source_message),
            dismissButtonText = stringResource(R.string.cancel),
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DialogOptionCard(
                        title = stringResource(R.string.choose_from_gallery),
                        icon = Icons.Default.PhotoLibrary,
                        onClick = {
                            showImageSourceDialog = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    DialogOptionCard(
                        title = stringResource(R.string.take_photo),
                        icon = Icons.Default.PhotoCamera,
                        onClick = {
                            showImageSourceDialog = false
                            val uri = FileUtils.getTempImageUri(context)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        }
                    )
                }
            }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(effect) { uiEffect ->
        when (uiEffect) {
            is UiEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(
                    message = uiEffect.message.asString(context),
                    actionLabel = uiEffect.actionLabel?.asString(context)
                )
            }
            is UiEffect.NavigateBack -> {
                onBookSavedSuccessfully()
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
        topBar = {
            Surface(
                shadowElevation = 6.dp,
                modifier = Modifier
                    .background(Color.Transparent)
                    .statusBarsPadding(),
            ) {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.add_book_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            onEvent(AddBookEvent.OnDiscard)
                            onNavigateBack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White
                    ),
                    actions = {
                        if (!state.bookDraft.isPhysical) {
                            IconButton(onClick = { showImportDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = stringResource(R.string.autocomplete_epub),
                                    tint = Color.White
                                )
                            }
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
                text = stringResource(R.string.book_details),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = state.bookDraft.title,
                onValueChange = { onEvent(AddBookEvent.OnTitleChanged(it)) },
                label = { Text(stringResource(R.string.title_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = state.bookDraft.author,
                onValueChange = { onEvent(AddBookEvent.OnAuthorChanged(it)) },
                label = { Text(stringResource(R.string.author_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = state.bookDraft.publisher,
                onValueChange = { onEvent(AddBookEvent.OnPublisherChanged(it)) },
                label = { Text(stringResource(R.string.publisher_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row (
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ){
                OutlinedTextField(
                    value = state.bookDraft.series,
                    onValueChange = { onEvent(AddBookEvent.OnSeriesChanged(it)) },
                    label = { Text(stringResource(R.string.series_label)) },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = state.bookDraft.seriesOrder,
                    onValueChange = { onEvent(AddBookEvent.OnSeriesOrderChanged(it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    label = { Text(stringResource(R.string.series_order_label)) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            GenreHybridSelector(
                availableGenres = state.availableGenres,
                selectedGenres = state.bookDraft.genres,
                onGenresChanged = { onEvent(AddBookEvent.OnGenresChanged(it)) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = stringResource(R.string.organization),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.format),
                style = MaterialTheme.typography.labelLarge,
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
                        checked = state.bookDraft.isPhysical,
                        onCheckedChange = { onEvent(AddBookEvent.OnIsPhysicalChanged(it)) }
                    )
                    Text(
                        text = stringResource(R.string.physical_book),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            FolderMultiSelector(
                availableFolders = state.availableFolders,
                selectedFolders = state.bookDraft.folders,
                onFoldersChanged = { onEvent(AddBookEvent.OnFoldersChanged(it)) },
                onAddFolderClick = { onNavigateToAddFolder() }
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = stringResource(R.string.files),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (state.bookDraft.isPhysical) Modifier
                        else Modifier.height(IntrinsicSize.Min)
                    ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .then(
                            if (state.bookDraft.isPhysical) Modifier.aspectRatio(0.7f)
                            else Modifier.fillMaxHeight()
                        )
                ) {
                    if (!state.bookDraft.coverUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = state.bookDraft.coverUrl,
                            contentDescription = stringResource(R.string.selected_cover),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(16.dp)
                                )
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
                        .then(
                            if (state.bookDraft.isPhysical) Modifier
                            else Modifier.fillMaxHeight()
                        ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!state.bookDraft.isPhysical) {
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
                                    text = stringResource(R.string.reading_file),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                val fileName =
                                    getFileNameFromUri(context, state.bookDraft.localFilePath)
                                Text(
                                    text = fileName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            basicDocumentLauncher.launch(arrayOf("application/epub+zip"))
                                        },
                                        modifier = Modifier
                                            .height(30.dp)
                                            .weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.secondary
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                                        contentPadding = PaddingValues(
                                            horizontal = 16.dp,
                                            vertical = 0.dp
                                        )
                                    ) {
                                        Text(
                                            text = if (state.bookDraft.localFilePath == null) stringResource(
                                                R.string.select
                                            ) else stringResource(R.string.change),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }

                                    if (state.bookDraft.localFilePath != null) {
                                        IconButton(
                                            onClick = {
                                                onEvent(
                                                    AddBookEvent.OnLocalFilePathChanged(
                                                        null
                                                    )
                                                )
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.remove_file),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showImageSourceDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (state.bookDraft.coverUrl == null) stringResource(R.string.add_cover) else stringResource(R.string.change_cover),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onEvent(AddBookEvent.OnSaveClick) },
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
                    Text(stringResource(R.string.save_book), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
