package com.juguito.juguitoreader.ui.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.ui.common.ObserveAsEvents
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.folder.components.FolderColorTonePicker
import com.juguito.juguitoreader.ui.theme.LoraFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    onNavigateBack: () -> Unit,
    onFolderSavedSuccessfully: () -> Unit,
    viewModel: FolderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            is UiEffect.NavigateBack -> onFolderSavedSuccessfully()
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
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        },
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                modifier = Modifier.statusBarsPadding()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (state.isEditing) "Editar Carpeta" else "Nueva Carpeta",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(state.colorHex.toColorInt()).copy(alpha = 0.18f))
                        .border(
                            width = 1.dp,
                            color = Color(state.colorHex.toColorInt()).copy(alpha = 0.35f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color(state.colorHex.toColorInt()),
                        modifier = Modifier.size(40.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.name.ifBlank { if (state.isEditing) "Modificar carpeta" else "Nueva carpeta" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = state.description.ifBlank { "Sin descripción" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(FolderEvent.OnNameChanged(it)) },
                label = { Text("Nombre") },
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onEvent(FolderEvent.OnDescriptionChanged(it)) },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            FolderColorTonePicker(
                selectedColorHex = state.colorHex,
                onColorSelected = { viewModel.onEvent(FolderEvent.OnColorChanged(it)) }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onEvent(FolderEvent.OnSaveClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(14.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = if (state.isEditing) "Guardar Cambios" else "Crear Carpeta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}