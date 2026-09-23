package com.juguito.juguitoreader.ui.reader.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.components.JuguitoDialog
import com.juguito.juguitoreader.ui.reader.ReaderEvent

@Composable
fun ChangeStatusPromptDialog(onEvent: (ReaderEvent) -> Unit) {
    JuguitoDialog(
        onDismissRequest = { onEvent(ReaderEvent.OnStatusPromptResult(false)) },
        icon = Icons.Default.AutoStories,
        title = stringResource(R.string.change_status_prompt_title),
        message = stringResource(R.string.change_status_prompt_message),
        confirmButtonText = stringResource(R.string.yes_change),
        dismissButtonText = stringResource(R.string.keep_same),
        onConfirm = { onEvent(ReaderEvent.OnStatusPromptResult(true)) }
    )
}