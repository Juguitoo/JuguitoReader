package com.juguito.juguitoreader.ui.book.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun AddBookScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddBookViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Añadir Nuevo Libro", style = MaterialTheme.typography.headlineMedium)

        // 1. Título
        OutlinedTextField(
            value = state.title,
            onValueChange = { viewModel.onEvent(AddBookEvent.OnTitleChanged(it)) },
            label = { Text("Título *") },
            modifier = Modifier.fillMaxWidth(),
            isError = state.errorMessage != null && state.title.isBlank()
        )

        // 2. Autor
        OutlinedTextField(
            value = state.author,
            onValueChange = { viewModel.onEvent(AddBookEvent.OnAuthorChanged(it)) },
            label = { Text("Autor *") },
            modifier = Modifier.fillMaxWidth()
        )

        // 3. Editorial
        OutlinedTextField(
            value = state.publisher,
            onValueChange = { viewModel.onEvent(AddBookEvent.OnPublisherChanged(it)) },
            label = { Text("Editorial") },
            modifier = Modifier.fillMaxWidth()
        )

        // 4. ¿Es Físico? (Un simple Checkbox + Texto)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(
                checked = state.isPhysical,
                onCheckedChange = { viewModel.onEvent(AddBookEvent.OnIsPhysicalChanged(it)) }
            )
            Text("Es un libro físico")
        }

        // Si hay error al guardar (por ejemplo los campos vacíos), lo mostramos
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Botón Guardar
        Button(
            onClick = { viewModel.onEvent(AddBookEvent.OnSaveClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Guardar Libro")
            }
        }
    }
}