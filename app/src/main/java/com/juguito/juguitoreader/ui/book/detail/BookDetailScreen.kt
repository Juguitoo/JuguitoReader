package com.juguito.juguitoreader.ui.book.detail

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.ui.book.components.BookInfoTab
import com.juguito.juguitoreader.ui.book.components.RegistryTab
import com.juguito.juguitoreader.ui.common.ObserveAsEvents
import com.juguito.juguitoreader.ui.common.components.DialogOptionCard
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.components.ErrorView
import com.juguito.juguitoreader.ui.components.JuguitoDialog
import com.juguito.juguitoreader.ui.registry.RegistryDatePickerDialog
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import com.juguito.juguitoreader.utils.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddFolder: () -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

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

    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            is UiEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(effect.message)
            }
            is UiEffect.NavigateBack -> {
                onNavigateBack()
            }
            else -> Unit
        }
    }

    if (showStartDatePicker && state is BookDetailUiState.Success) {
        val successState = state as BookDetailUiState.Success
        RegistryDatePickerDialog(
            initialDate = successState.startDate,
            onDateSelected = {
                viewModel.onEvent(BookDetailEvent.OnStartDateChanged(it))
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker && state is BookDetailUiState.Success) {
        val successState = state as BookDetailUiState.Success
        RegistryDatePickerDialog(
            initialDate = successState.endDate,
            onDateSelected = {
                viewModel.onEvent(BookDetailEvent.OnEndDateChanged(it))
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    if (showDeleteDialog && state is BookDetailUiState.Success) {
        val successState = state as BookDetailUiState.Success
        JuguitoDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = Icons.Default.Delete,
            title = "Eliminar libro",
            message = "¿Estás seguro de que deseas eliminar '${successState.book.title}'? Esta acción no se puede deshacer.",
            confirmButtonText = "Eliminar",
            isDestructive = true,
            onConfirm = {
                showDeleteDialog = false
                viewModel.onEvent(BookDetailEvent.OnDeleteClick)
            }
        )
    }

    if (showImageSourceDialog) {
        JuguitoDialog(
            onDismissRequest = { showImageSourceDialog = false },
            icon = Icons.Default.AddPhotoAlternate,
            title = "Seleccionar portada",
            message = "¿Cómo quieres añadir la imagen para el libro?",
            dismissButtonText = "Cancelar",
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DialogOptionCard(
                        title = "Elegir de la galería",
                        icon = Icons.Default.PhotoLibrary,
                        onClick = {
                            showImageSourceDialog = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    DialogOptionCard(
                        title = "Tomar una foto",
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
            val titleText = when (val s = state) {
                is BookDetailUiState.Success -> if (s.isEditMode) "Editar Libro" else s.bookDraft.title
                else -> "Detalle del libro"
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
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    val s = state
                    if (s is BookDetailUiState.Success) {
                        if (s.isEditMode) {
                            IconButton(onClick = { viewModel.onEvent(BookDetailEvent.OnEditModeChanged(false)) }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancelar", tint = Color.White)
                            }
                            IconButton(onClick = { viewModel.onEvent(BookDetailEvent.OnSaveClick) }) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Guardar", tint = Color.White)
                            }
                        } else {
                            IconButton(onClick = { viewModel.onEvent(BookDetailEvent.OnEditModeChanged(true)) }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = Color.White)
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
            when (val uiState = state) {
                is BookDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is BookDetailUiState.Error -> {
                    ErrorView(
                        message = uiState.message,
                        onRetry = { viewModel.loadBook() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is BookDetailUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        PrimaryTabRow(selectedTabIndex = uiState.selectedTab) {
                            Tab(
                                selected = uiState.selectedTab == 0,
                                onClick = { viewModel.onEvent(BookDetailEvent.OnTabChanged(0)) },
                                text = { Text("Información") }
                            )
                            Tab(
                                selected = uiState.selectedTab == 1,
                                onClick = { viewModel.onEvent(BookDetailEvent.OnTabChanged(1)) },
                                text = { Text("Registro") }
                            )
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            if (uiState.selectedTab == 0) {
                                BookInfoTab(
                                    state = uiState,
                                    onEvent = viewModel::onEvent,
                                    onPickCover = { showImageSourceDialog = true },
                                    onPickFile = {
                                        documentPickerLauncher.launch(
                                            arrayOf(
                                                "application/epub+zip",
                                                "application/pdf"
                                            )
                                        )
                                    },
                                    onDeleteRequest = { showDeleteDialog = true },
                                    onAddFolderClick = { onNavigateToAddFolder() }
                                )
                            } else {
                                RegistryTab(
                                    state = uiState,
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
    }
}

