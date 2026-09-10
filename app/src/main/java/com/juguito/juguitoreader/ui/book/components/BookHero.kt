package com.juguito.juguitoreader.ui.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.book.detail.BookDetailUiState
import com.juguito.juguitoreader.ui.theme.LoraFontFamily

@Composable
fun BookHero(
    state: BookDetailUiState.Success,
    onReadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val draft = state.bookDraft
    val canRead = !draft.isPhysical && !draft.localFilePath.isNullOrBlank()
    val progress = state.book.readingProgress?.percentage

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            if (draft.coverUrl != null) {
                AsyncImage(
                    model = draft.coverUrl,
                    contentDescription = stringResource(R.string.book_cover),
                    modifier = Modifier
                        .width(120.dp)
                        .aspectRatio(0.7f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .width(120.dp)
                        .aspectRatio(0.7f),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = stringResource(R.string.book_cover),
                            modifier = Modifier.padding(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = draft.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = LoraFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.by_author, draft.author),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (draft.series.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.saga_volume, draft.series, draft.seriesOrder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (draft.publisher.isNotBlank()) {
                    Text(
                        text = draft.publisher,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val formatText = if (draft.isPhysical) {
                    stringResource(R.string.physical_format)
                } else {
                    stringResource(R.string.digital_format)
                }
                val formatIcon =
                    if (draft.isPhysical) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.Devices

                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(formatText) },
                    leadingIcon = {
                        Icon(
                            formatIcon,
                            contentDescription = null,
                            modifier = Modifier.width(18.dp)
                        )
                    }
                )
            }
        }

        if (canRead && progress != null) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Text(
                    text = stringResource(R.string.progress_percent, progress),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (canRead) {
            Button(
                onClick = onReadClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if ((progress ?: 0) > 0) {
                        stringResource(R.string.continue_reading)
                    } else {
                        stringResource(R.string.read_book)
                    }
                )
            }
        }
    }
}
