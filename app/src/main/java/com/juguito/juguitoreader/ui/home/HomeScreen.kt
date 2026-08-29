package com.juguito.juguitoreader.ui.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.juguito.juguitoreader.R
import androidx.compose.runtime.LaunchedEffect
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
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.ui.common.ObserveAsEvents
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.components.EmptyLibraryView
import com.juguito.juguitoreader.ui.components.ErrorView
import com.juguito.juguitoreader.ui.home.components.BookListSection
import com.juguito.juguitoreader.ui.theme.LoraFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    snackbarMessage: String?,
    onClearSnackbarMessage: () -> Unit,
    onNavigateToAddBook: () -> Unit,
    onNavigateToBookDetail: (Int) -> Unit,
    onNavigateToReadBook: (Int) -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.uiState.collectAsState()

    HomeContent(
        modifier = modifier,
        state = homeState,
        snackbarMessage = snackbarMessage,
        onClearSnackbarMessage = onClearSnackbarMessage,
        onNavigateToAddBook = onNavigateToAddBook,
        onNavigateToBookDetail = onNavigateToBookDetail,
        onNavigateToReadBook = onNavigateToReadBook,
        onOpenDrawer = onOpenDrawer,
        onEvent = viewModel::onEvent,
        effect = viewModel.effect
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    state: HomeUiState,
    snackbarMessage: String?,
    onClearSnackbarMessage: () -> Unit,
    onNavigateToAddBook: () -> Unit,
    onNavigateToBookDetail: (Int) -> Unit,
    onNavigateToReadBook: (Int) -> Unit,
    onOpenDrawer: () -> Unit,
    onEvent: (HomeEvent) -> Unit,
    effect: kotlinx.coroutines.flow.Flow<UiEffect>
) {
    val context = LocalContext.current
    var bookToDelete by remember { mutableStateOf<Book?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    if (bookToDelete != null) {
        bookToDelete?.let {
            onEvent(HomeEvent.OnDeleteBookClick(it))
        }
        bookToDelete = null
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearSnackbarMessage()
        }
    }

    ObserveAsEvents(effect) { uiEffect ->
        when (uiEffect) {
            is UiEffect.ShowSnackbar -> {
                val result = snackbarHostState.showSnackbar(
                    message = uiEffect.message.asString(context),
                    actionLabel = uiEffect.actionLabel?.asString(context),
                    duration = SnackbarDuration.Short
                )

                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        if (uiEffect.actionPayload == "undo_delete") {
                            onEvent(HomeEvent.OnUndoDeleteClick)
                        }
                    }
                    SnackbarResult.Dismissed -> {
                        if (uiEffect.actionPayload == "undo_delete") {
                            onEvent(HomeEvent.OnDeleteConfirmed)
                        }
                    }
                }
            }
            else -> Unit
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                onEvent(HomeEvent.OnImportBook(it))
            }
        }
    )

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
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.menu),
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            documentPickerLauncher.launch(arrayOf("application/epub+zip"))
                        }) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = stringResource(R.string.import_epub),
                                tint = Color.White
                            )
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

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is HomeUiState.Error -> {
                    ErrorView(
                        message = state.message.asString(),
                        onRetry = { onEvent(HomeEvent.OnDismissError) }
                    )
                }
                is HomeUiState.Empty -> {
                    EmptyLibraryView(
                        onNavigateToAddBook = onNavigateToAddBook,
                        onImportClick = {
                            documentPickerLauncher.launch(arrayOf("application/epub+zip"))
                        }
                    )
                }
                is HomeUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {

                                val hasReading = state.readingBooks.isNotEmpty()
                                val hasPending = state.pendingBooks.isNotEmpty()

                                if (!hasReading && !hasPending) {
                                    BookListSection(
                                        title = stringResource(R.string.your_shelf),
                                        books = emptyList(),
                                        showActions = true,
                                        onNavigateToAddBook = onNavigateToAddBook,
                                        onImportClick = {
                                            documentPickerLauncher.launch(
                                                arrayOf(
                                                    "application/epub+zip"
                                                )
                                            )
                                        },
                                        onBookDetails = onNavigateToBookDetail,
                                        onDeleteBook = { bookToDelete = it },
                                        onReadBook = onNavigateToReadBook
                                    )
                                }
                                else {
                                    if (hasReading) {
                                        BookListSection(
                                            title = stringResource(R.string.continue_reading),
                                            books = state.readingBooks,
                                            onBookDetails = onNavigateToBookDetail,
                                            onDeleteBook = { bookToDelete = it },
                                            onReadBook = onNavigateToReadBook
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(12.dp)
                                                .background(Color(0xFF5F4934))
                                        )
                                    }

                                    BookListSection(
                                        title = stringResource(R.string.pending_books),
                                        books = state.pendingBooks,
                                        showActions = true,
                                        onNavigateToAddBook = onNavigateToAddBook,
                                        onImportClick = {
                                            documentPickerLauncher.launch(
                                                arrayOf(
                                                    "application/epub+zip"
                                                )
                                            )
                                        },
                                        onBookDetails = onNavigateToBookDetail,
                                        onDeleteBook = { bookToDelete = it },
                                        onReadBook = onNavigateToReadBook
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .background(Color(0xFF5F4934))
                                )
                            }
                        }
/*
                        item {
                            Text(
                                text = "Estadísticas generales".uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                letterSpacing = 1.sp
                            )
                            StatsSection(statsUiState = state.stats)
                        }

 */
                    }
                }
            }
        }
    }
}

