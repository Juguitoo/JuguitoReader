package com.juguito.juguitoreader.ui.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.ui.home.components.ActionBookCard
import com.juguito.juguitoreader.ui.home.components.BookListSection
import com.juguito.juguitoreader.ui.home.components.StatsSection
import com.juguito.juguitoreader.ui.theme.LoraFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddBook: () -> Unit,
    onNavigateToBookDetail: (Int) -> Unit,
    onNavigateToReadBook: (Int) -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var bookToDelete by remember { mutableStateOf<Book?>(null) }

    if (bookToDelete != null) {
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            title = { Text("Eliminar libro") },
            text = { Text("¿Estás seguro de que quieres eliminar '${bookToDelete?.title}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        bookToDelete?.let { viewModel.deleteBook(it.id) }
                        bookToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                viewModel.importBook(it)
            }
        }
    )

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 6.dp,
                modifier = Modifier.statusBarsPadding()
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "JuguitoReader",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            documentPickerLauncher.launch(arrayOf("application/epub+zip", "application/pdf"))
                        }) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "Importar EPUB",
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
            when (val state = homeState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is HomeUiState.Error -> {
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
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                is HomeUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tu biblioteca está vacía",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Añade tu primer libro para empezar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ActionBookCard(
                                title = "Nuevo",
                                icon = Icons.Default.Add,
                                width = 120.dp,
                                onClick = onNavigateToAddBook
                            )
                            ActionBookCard(
                                title = "Importar",
                                icon = Icons.Default.UploadFile,
                                width = 120.dp,
                                onClick = {
                                    documentPickerLauncher.launch(
                                        arrayOf(
                                            "application/epub+zip",
                                            "application/pdf"
                                        )
                                    )
                                }
                            )
                        }
                    }
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
                                        title = "Tu estantería",
                                        books = emptyList(),
                                        showActions = true,
                                        onNavigateToAddBook = onNavigateToAddBook,
                                        onImportClick = {
                                            documentPickerLauncher.launch(
                                                arrayOf(
                                                    "application/epub+zip",
                                                    "application/pdf"
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
                                            title = "Continuar lectura",
                                            books = state.readingBooks,
                                            onBookDetails = onNavigateToBookDetail,
                                            onDeleteBook = { bookToDelete = it },
                                            onReadBook = onNavigateToReadBook
                                        )
                                    }

                                    BookListSection(
                                        title = "Libros pendientes",
                                        books = state.pendingBooks,
                                        showActions = true,
                                        onNavigateToAddBook = onNavigateToAddBook,
                                        onImportClick = {
                                            documentPickerLauncher.launch(
                                                arrayOf(
                                                    "application/epub+zip",
                                                    "application/pdf"
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
                                        .height(8.dp)
                                        .background(MaterialTheme.colorScheme.secondary)
                                )
                            }
                        }

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
                    }
                }
            }
        }
    }
}

