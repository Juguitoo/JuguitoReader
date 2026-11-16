package com.juguito.juguitoreader.ui.reader.bookmark

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookmarkColor
import com.juguito.juguitoreader.domain.enums.BookmarkKind
import com.juguito.juguitoreader.domain.usecase.bookmark.MAX_BOOKMARK_NOTE
import com.juguito.juguitoreader.domain.usecase.bookmark.NewBookmark
import com.juguito.juguitoreader.ui.components.JuguitoDialog
import com.juguito.juguitoreader.ui.reader.ReaderEvent
import com.juguito.juguitoreader.ui.reader.ReaderUiState

@Composable
fun BookmarkNoteDialog(
    state: ReaderUiState.Success,
    onEvent: (ReaderEvent) -> Unit
) {
    val existing = state.selectedBookmark?.takeIf { it.kind != BookmarkKind.POSITION }
    val selection = state.pendingSelection
    if (existing == null && (!state.showNewNote || selection == null)) return

    var text by remember(existing?.id, state.showNewNote) {
        mutableStateOf(existing?.noteText.orEmpty())
    }
    var color by remember(existing?.id, state.showNewNote) {
        mutableStateOf(existing?.color ?: BookmarkColor.AMBER)
    }
    val isImage = existing?.kind == BookmarkKind.IMAGE
    val title = stringResource(
        when {
            existing == null -> R.string.bookmark_note
            isImage -> R.string.bookmark_image_caption
            else -> R.string.edit_bookmark_note
        }
    )

    JuguitoDialog(
        onDismissRequest = {
            if (existing == null) onEvent(ReaderEvent.OnCloseNewNote)
            else onEvent(ReaderEvent.OnCloseNote)
        },
        title = title,
        message = "",
        confirmButtonText = stringResource(R.string.save),
        confirmEnabled = isImage || text.isNotBlank(),
        onConfirm = {
            if (existing == null && selection != null) {
                onEvent(
                    ReaderEvent.OnAddBookmark(
                        NewBookmark.Note(
                            bookId = 0,
                            chapterHref = "",
                            quote = selection.quote,
                            prefix = selection.prefix,
                            suffix = selection.suffix,
                            scrollPosition = selection.scrollPosition,
                            color = color,
                            noteText = text
                        )
                    )
                )
            } else if (existing != null) {
                onEvent(ReaderEvent.OnEditBookmark(existing.id, color, text))
            }
        },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!isImage) BookmarkColorRow(selected = color, onColor = { color = it })
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= MAX_BOOKMARK_NOTE) text = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            stringResource(
                                if (isImage) R.string.bookmark_image_caption_hint else R.string.bookmark_note_hint
                            )
                        )
                    },
                    minLines = 3
                )
            }
        }
    )
}

@Composable
internal fun BookmarkColorRow(
    selected: BookmarkColor?,
    onColor: (BookmarkColor) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val dot = if (compact) 22.dp else 28.dp
    val hit = if (compact) 32.dp else 48.dp
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BookmarkColor.entries.forEach { color ->
            val label = stringResource(color.labelRes())
            IconButton(
                onClick = { onColor(color) },
                modifier = Modifier
                    .size(hit)
                    .semantics { contentDescription = label }
            ) {
                val ring = if (selected == color) {
                    Modifier
                        .size(dot)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(3.dp)
                } else {
                    Modifier.size(dot)
                }
                Box(
                    modifier = ring
                        .clip(CircleShape)
                        .background(color.swatch())
                )
            }
        }
    }
}
