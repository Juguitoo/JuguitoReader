package com.juguito.juguitoreader.ui.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Book

@Composable
fun BookActionsMenu(
    expanded: Boolean,
    book: Book,
    onDismiss: () -> Unit,
    onRead: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
) {
    val canRead = !book.isPhysical && !book.localFilePath.isNullOrBlank()

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        if (canRead) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.read_book)) },
                onClick = {
                    onDismiss()
                    onRead()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null
                    )
                }
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.details)) },
            onClick = {
                onDismiss()
                onDetails()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            },
            onClick = {
                onDismiss()
                onDelete()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}
