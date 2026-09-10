package com.juguito.juguitoreader.ui.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.book.detail.BookDetailUiState

@Composable
fun CatalogSummary(
    state: BookDetailUiState.Success,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val draft = state.bookDraft
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.technical_sheet),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onEditClick) {
                Text(stringResource(R.string.edit))
            }
        }

        if (draft.genres.isNotEmpty()) {
            Text(
                text = stringResource(R.string.genres),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                draft.genres.forEach { genre ->
                    SuggestionChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(genre.name) }
                    )
                }
            }
        }

        if (draft.folders.isNotEmpty()) {
            Text(
                text = stringResource(R.string.folders),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                draft.folders.forEach { folder ->
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(folder.name) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(folder.colorHex.toColorInt())
                            )
                        }
                    )
                }
            }
        }
    }
}

