package com.juguito.juguitoreader.ui.genre

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.ui.components.JuguitoDialog

@Composable
fun AddGenreDialog(
    onDismissRequest: () -> Unit,
    viewModel: AddGenreViewModel = hiltViewModel()
) {
    var genreName by remember { mutableStateOf("") }
    var isLocalEmptyError by remember { mutableStateOf(false) }

    val dbError by viewModel.error.collectAsState()

    val isError = isLocalEmptyError || dbError != null
    val errorMessage = when {
        isLocalEmptyError -> "El nombre no puede estar vacío"
        dbError != null -> dbError
        else -> null
    }

    JuguitoDialog(
        onDismissRequest = onDismissRequest,
        icon = Icons.AutoMirrored.Filled.Label,
        title = "Nuevo Género",
        message = "Escribe el nombre del nuevo género para tu biblioteca.",
        confirmButtonText = "Crear",
        dismissButtonText = "Cancelar",
        onConfirm = {
            if (genreName.isNotBlank()) {
                viewModel.saveGenre(genreName.trim()) {
                    onDismissRequest()
                }
            } else {
                isLocalEmptyError = true
            }
        },
        content = {
            OutlinedTextField(
                value = genreName,
                onValueChange = {
                    genreName = it
                    isLocalEmptyError = false
                    viewModel.clearError()
                },
                label = { Text("Nombre del género") },
                isError = isError,
                supportingText = errorMessage?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    )
}