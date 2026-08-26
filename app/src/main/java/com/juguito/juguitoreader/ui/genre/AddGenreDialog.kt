package com.juguito.juguitoreader.ui.genre

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.R
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
        isLocalEmptyError -> stringResource(R.string.name_empty_error)
        dbError != null -> dbError?.asString()
        else -> null
    }

    JuguitoDialog(
        onDismissRequest = onDismissRequest,
        icon = Icons.AutoMirrored.Filled.Label,
        title = stringResource(R.string.new_genre_title),
        message = stringResource(R.string.new_genre_message),
        confirmButtonText = stringResource(R.string.create),
        dismissButtonText = stringResource(R.string.cancel),
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
                label = { Text(stringResource(R.string.genre_name_label)) },
                isError = isError,
                supportingText = errorMessage?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    )
}