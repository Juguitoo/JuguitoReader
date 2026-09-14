package com.juguito.juguitoreader.ui.book.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.book.detail.BookDetailEvent
import com.juguito.juguitoreader.ui.book.detail.BookDetailUiState
import com.juguito.juguitoreader.utils.FileUtils

@Composable
fun BookInfoTab(
    state: BookDetailUiState.Success,
    onEvent: (BookDetailEvent) -> Unit,
    onPickCover: () -> Unit,
    onPickFile: () -> Unit,
    onAddFolderClick: () -> Unit
) {
    val context = LocalContext.current
    val draft = state.bookDraft
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.technical_sheet),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp, 150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onPickCover() },
                contentAlignment = Alignment.Center
            ) {
                if (draft.coverUrl != null) {
                    AsyncImage(
                        model = draft.coverUrl,
                        contentDescription = stringResource(R.string.book_cover),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = stringResource(R.string.change_cover),
                        tint = Color.White
                    )
                }
            }

            if (!draft.isPhysical) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onPickFile, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.FileUpload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.change))
                        }
                        if (draft.localFilePath != null) {
                            IconButton(onClick = {
                                onEvent(BookDetailEvent.OnLocalFilePathChanged(null))
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.remove_file),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    val fileName = FileUtils.getFileNameFromUri(context, draft.localFilePath)
                    Text(text = fileName, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
            }
        }

        OutlinedTextField(
            value = draft.title,
            onValueChange = { onEvent(BookDetailEvent.OnTitleChanged(it)) },
            label = { Text(stringResource(R.string.title_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = draft.author,
            onValueChange = { onEvent(BookDetailEvent.OnAuthorChanged(it)) },
            label = { Text(stringResource(R.string.author_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = draft.publisher,
            onValueChange = { onEvent(BookDetailEvent.OnPublisherChanged(it)) },
            label = { Text(stringResource(R.string.publisher_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = draft.series,
                onValueChange = { onEvent(BookDetailEvent.OnSeriesChanged(it)) },
                label = { Text(stringResource(R.string.series_label)) },
                modifier = Modifier.weight(1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = draft.seriesOrder,
                onValueChange = { onEvent(BookDetailEvent.OnSeriesOrderChanged(it)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                label = { Text(stringResource(R.string.series_order_label)) },
                modifier = Modifier.width(90.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
        }

        GenreHybridSelector(
            availableGenres = state.availableGenres,
            selectedGenres = draft.genres,
            onGenresChanged = { onEvent(BookDetailEvent.OnGenresChanged(it)) }
        )

        FolderMultiSelector(
            availableFolders = state.availableFolders,
            selectedFolders = draft.folders,
            onFoldersChanged = { onEvent(BookDetailEvent.OnFoldersChanged(it)) },
            onAddFolderClick = { onAddFolderClick() }
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = draft.isPhysical,
                    onCheckedChange = { onEvent(BookDetailEvent.OnIsPhysicalChanged(it)) }
                )
                Text(
                    text = stringResource(R.string.physical_book),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}