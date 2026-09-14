package com.juguito.juguitoreader.ui.book.detail

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.book.components.BookHero
import com.juguito.juguitoreader.ui.book.components.BookInfoTab
import com.juguito.juguitoreader.ui.book.components.CatalogSummary
import com.juguito.juguitoreader.ui.book.components.ReadingJournal
import com.juguito.juguitoreader.ui.common.ObserveAsEvents
import com.juguito.juguitoreader.ui.common.components.DialogOptionCard
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.components.ErrorView
import com.juguito.juguitoreader.ui.components.JuguitoDialog
import com.juguito.juguitoreader.ui.common.components.JuguitoDatePickerDialog
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import com.juguito.juguitoreader.utils.FileUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddFolder: () -> Unit,
    onNavigateToReadBook: (Int) -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    BookDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToAddFolder = onNavigateToAddFolder,
        onNavigateToReadBook = onNavigateToReadBook,
        onLoadBook = viewModel::loadBook,
        effect = viewModel.effect
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailContent(
    state: BookDetailUiState,
    onEvent: (BookDetailEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToAddFolder: () -> Unit,
    onNavigateToReadBook: (Int) -> Unit,
    onLoadBook: () -> Unit,
    effect: kotlinx.coroutines.flow.Flow<UiEffect>
) {
    val context = LocalContext.current
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }

    DisposableEffect(Unit) {
        onDispose { onEvent(BookDetailEvent.OnFlushJournal) }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                onEvent(BookDetailEvent.OnCoverChanged(it.toString()))
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempCameraFile?.let { onEvent(BookDetailEvent.OnCoverChanged(it.absolutePath)) }
            } else {
                tempCameraFile?.delete()
            }
            tempCameraFile = null
        }
    )

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                onEvent(BookDetailEvent.OnEpubFilePicked(it))
            }
        }
    )

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
                onNavigateBack()
            }
            else -> Unit
        }
    }

    BackHandler { onEvent(BookDetailEvent.OnDiscard) }

    if (showStartDatePicker && state is BookDetailUiState.Success) {
        JuguitoDatePickerDialog(
            initialDate = state.startDate,
            onDateSelected = { onEvent(BookDetailEvent.OnStartDateChanged(it)) },
            onDismiss = { showStartDatePicker = false },
            onClear = { onEvent(BookDetailEvent.OnStartDateChanged(null)) }
        )
    }

    if (showEndDatePicker && state is BookDetailUiState.Success) {
        JuguitoDatePickerDialog(
            initialDate = state.endDate,
            onDateSelected = { onEvent(BookDetailEvent.OnEndDateChanged(it)) },
            onDismiss = { showEndDatePicker = false },
            onClear = { onEvent(BookDetailEvent.OnEndDateChanged(null)) }
        )
    }

    if (showDeleteDialog && state is BookDetailUiState.Success) {
        JuguitoDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = Icons.Default.Delete,
            title = stringResource(R.string.delete_book_title),
            message = stringResource(R.string.delete_book_confirmation, state.book.title),
            confirmButtonText = stringResource(R.string.delete),
            isDestructive = true,
            onConfirm = {
                showDeleteDialog = false
                onEvent(BookDetailEvent.OnDeleteClick)
            }
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
                            val file = FileUtils.createTempImageFile(context)
                            tempCameraFile = file
                            cameraLauncher.launch(FileUtils.getUriForFile(context, file))
                        }
                    )
                }
            }
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
        modifier = Modifier.imePadding(),
        topBar = {
            val titleText = when (state) {
                is BookDetailUiState.Success -> if (state.isEditMode) {
                    stringResource(R.string.edit_book)
                } else {
                    state.bookDraft.title
                }
                else -> stringResource(R.string.book_detail)
            }
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = LoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(BookDetailEvent.OnDiscard) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (state is BookDetailUiState.Success) {
                        if (state.isEditMode) {
                            IconButton(onClick = { onEvent(BookDetailEvent.OnEditModeChanged(false)) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.cancel),
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = { onEvent(BookDetailEvent.OnSaveClick) },
                                enabled = !state.isActionLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(R.string.save),
                                    tint = Color.White
                                )
                            }
                        } else {
                            Box {
                                IconButton(onClick = { showOverflowMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = stringResource(R.string.options),
                                        tint = Color.White
                                    )
                                }
                                DropdownMenu(
                                    expanded = showOverflowMenu,
                                    onDismissRequest = { showOverflowMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.edit_book)) },
                                        onClick = {
                                            showOverflowMenu = false
                                            onEvent(BookDetailEvent.OnEditModeChanged(true))
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.delete)) },
                                        onClick = {
                                            showOverflowMenu = false
                                            showDeleteDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
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
                is BookDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is BookDetailUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { onLoadBook() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is BookDetailUiState.Success -> {
                    if (state.isEditMode) {
                        BookInfoTab(
                            state = state,
                            onEvent = onEvent,
                            onPickCover = { showImageSourceDialog = true },
                            onPickFile = {
                                documentPickerLauncher.launch(
                                    arrayOf("application/epub+zip")
                                )
                            },
                            onAddFolderClick = { onNavigateToAddFolder() }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState())
                                .imePadding(),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            BookHero(
                                state = state,
                                onReadClick = { onNavigateToReadBook(state.book.id) }
                            )
                            HorizontalDivider()
                            ReadingJournal(
                                state = state,
                                onEvent = onEvent,
                                onShowStartDatePicker = { showStartDatePicker = true },
                                onShowEndDatePicker = { showEndDatePicker = true }
                            )
                            HorizontalDivider()
                            CatalogSummary(
                                state = state,
                                onEditClick = { onEvent(BookDetailEvent.OnEditModeChanged(true)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
