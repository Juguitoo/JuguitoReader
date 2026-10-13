package com.juguito.juguitoreader.ui.library.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.ui.common.toUiText

@Composable
fun BookLibraryActionsMenu(
    expanded: Boolean,
    book: Book,
    onDismiss: () -> Unit,
    onDetails: () -> Unit,
    onStatusChange: (BookStatus) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.details)) },
            onClick = {
                onDismiss()
                onDetails()
            },
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
        )

        HorizontalDivider()

        Text(
            stringResource(R.string.change_status),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        BookStatus.entries.forEach { status ->
            DropdownMenuItem(
                text = { Text(status.toUiText().asString()) },
                onClick = {
                    onDismiss()
                    onStatusChange(status)
                },
                leadingIcon = {
                    if (book.status == status) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )
        }
    }
}
