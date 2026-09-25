package com.juguito.juguitoreader.ui.reader

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.components.JuguitoDialog

@Composable
fun ReaderGuideDialog(
    onDismiss: () -> Unit
) {
    JuguitoDialog(
        onDismissRequest = onDismiss,
        icon = Icons.AutoMirrored.Filled.MenuBook,
        title = stringResource(R.string.reader_guide_title),
        message = stringResource(R.string.reader_guide_message),
        confirmButtonText = stringResource(R.string.whats_new_got_it),
        onConfirm = onDismiss,
        dismissButtonText = null
    )
}
