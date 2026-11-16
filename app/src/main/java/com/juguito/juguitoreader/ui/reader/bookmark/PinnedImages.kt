package com.juguito.juguitoreader.ui.reader.bookmark

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookmarkKind
import com.juguito.juguitoreader.domain.model.Bookmark
import com.juguito.juguitoreader.ui.reader.ReaderEvent
import com.juguito.juguitoreader.ui.reader.ReaderUiState
import com.juguito.juguitoreader.ui.theme.JuguitoPrimary
import com.juguito.juguitoreader.utils.EpubImagePreview
import com.juguito.juguitoreader.utils.decodeEpubImage
import com.juguito.juguitoreader.utils.isPreviewableEpubImage
import com.juguito.juguitoreader.utils.loadEpubImagePreview
import com.juguito.juguitoreader.utils.resolveEpubImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun ColumnScope.PinnedImagePage(
    state: ReaderUiState.Success,
    onEvent: (ReaderEvent) -> Unit
) {
    val images = remember(state.bookmarks) {
        state.bookmarks.filter { it.kind == BookmarkKind.IMAGE }.sortedByDescending { it.createdAt }
    }
    if (images.isEmpty()) {
        Text(
            text = stringResource(R.string.bookmark_images_empty),
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            itemsIndexed(images, key = { _, mark -> mark.id }) { _, mark ->
                PinnedImageItem(
                    baseDir = state.epubContent.baseDir,
                    mark = mark,
                    onOpen = { onEvent(ReaderEvent.OnOpenPinnedImage(mark.id)) },
                    onEdit = { onEvent(ReaderEvent.OnOpenNote(mark.id)) },
                    onDelete = { onEvent(ReaderEvent.OnDeleteBookmark(mark.id)) }
                )
            }
        }
    }
}

@Composable
private fun PinnedImageItem(
    baseDir: String,
    mark: Bookmark,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val file = remember(baseDir, mark.chapterHref, mark.imageSrc) {
        resolveEpubImage(baseDir, mark.chapterHref, mark.imageSrc.orEmpty())
    }
    val preview by produceState<EpubImagePreview?>(initialValue = null, file) {
        value = if (file != null && isPreviewableEpubImage(file)) {
            withContext(Dispatchers.IO) { loadEpubImagePreview(file, 1600) }
        } else {
            null
        }
    }
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatBookmarkTime(mark.createdAt),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.bookmark_image_caption),
                    modifier = Modifier.size(16.dp),
                    tint = JuguitoPrimary
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        val decoded = preview
        if (decoded != null) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val density = LocalDensity.current
                val naturalWidth = with(density) { decoded.widthPx.toDp() }
                val naturalHeight = with(density) { decoded.heightPx.toDp() }
                val scale = if (naturalWidth > maxWidth) maxWidth / naturalWidth else 1f
                Image(
                    bitmap = decoded.bitmap.asImageBitmap(),
                    contentDescription = mark.noteText,
                    modifier = Modifier.size(naturalWidth * scale, naturalHeight * scale),
                    contentScale = ContentScale.FillBounds
                )
            }
        }

        if (!mark.noteText.isNullOrBlank()) {
            Text(
                text = mark.noteText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PinnedImageViewer(
    state: ReaderUiState.Success,
    onEvent: (ReaderEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val mark = state.bookmarks.firstOrNull { it.id == state.openImageId } ?: return
    val file = remember(state.epubContent.baseDir, mark.chapterHref, mark.imageSrc) {
        resolveEpubImage(state.epubContent.baseDir, mark.chapterHref, mark.imageSrc.orEmpty())
    }
    val bitmap by produceState<Bitmap?>(initialValue = null, file) {
        value = if (file != null && isPreviewableEpubImage(file)) {
            withContext(Dispatchers.IO) { decodeEpubImage(file, 1600) }
        } else {
            null
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val decoded = bitmap
        if (decoded != null) {
            Image(
                bitmap = decoded.asImageBitmap(),
                contentDescription = mark.listTitle(),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = stringResource(R.string.bookmark_image_unavailable),
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
        IconButton(
            onClick = { onEvent(ReaderEvent.OnClosePinnedImage) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.close),
                tint = Color.White
            )
        }
    }
}
