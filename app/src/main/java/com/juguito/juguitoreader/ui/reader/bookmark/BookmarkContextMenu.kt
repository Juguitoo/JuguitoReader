package com.juguito.juguitoreader.ui.reader.bookmark

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.usecase.bookmark.NewBookmark
import com.juguito.juguitoreader.ui.reader.MenuAnchor
import com.juguito.juguitoreader.ui.reader.ReaderEvent
import com.juguito.juguitoreader.ui.reader.ReaderUiState
import com.juguito.juguitoreader.ui.reader.components.SystemMenuAction
import kotlin.math.roundToInt

@Composable
fun BookmarkContextMenu(
    state: ReaderUiState.Success,
    systemActions: List<SystemMenuAction>,
    onSystemAction: (Int) -> Unit,
    onEvent: (ReaderEvent) -> Unit
) {
    if (state.showBookmarkList || state.showNewNote || state.selectedBookmark != null) return
    val mark = state.bookmarks.firstOrNull { it.id == state.markMenuId }
    val selection = state.pendingSelection
    val anchor = when {
        mark != null -> state.markMenuAnchor
        selection != null -> selection.anchor
        else -> null
    } ?: return

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (mark != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onEvent(ReaderEvent.OnCloseMarkMenu) }
                    )
            )
        }
        var menuSize by remember { mutableStateOf(IntSize.Zero) }
        val density = LocalDensity.current
        Popup(
            alignment = Alignment.TopStart,
            offset = placeMenu(anchor, menuSize, constraints.maxWidth, constraints.maxHeight, density.density),
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                clippingEnabled = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = maxWidth - 16.dp)
                    .onGloballyPositioned { menuSize = it.size },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 3.dp
            ) {
                val context = LocalContext.current
                val markText = mark?.quote.orEmpty()
                var showColors by remember(mark?.id, selection?.quote) { mutableStateOf(false) }
                var moreOpen by remember(mark?.id, selection?.quote) { mutableStateOf(false) }
                Column {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showColors = !showColors },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.highlight),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (showColors) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            val swatch = mark?.color?.swatch()
                            if (swatch != null) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(swatch)
                                )
                            }
                        }
                        IconButton(onClick = {
                            if (mark != null) onEvent(ReaderEvent.OnOpenNote(mark.id))
                            else onEvent(ReaderEvent.OnOpenNewNote)
                        }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.bookmark_note),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (mark != null) {
                            IconButton(onClick = { onEvent(ReaderEvent.OnDeleteBookmark(mark.id)) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (mark != null && markText.isNotBlank()) {
                                TextAction(stringResource(R.string.copy)) { copyText(context, markText) }
                                TextAction(stringResource(R.string.share)) { shareText(context, markText) }
                            } else {
                                val copyAction = systemActions.firstOfKind(TextActionKind.COPY, context)
                                val shareAction = systemActions.firstOfKind(TextActionKind.SHARE, context)
                                val selectAllAction = systemActions.firstOfKind(TextActionKind.SELECT_ALL, context)
                                listOfNotNull(copyAction, shareAction, selectAllAction).forEach { action ->
                                    TextAction(action.title) { onSystemAction(action.id) }
                                }
                            }
                        }
                        if (mark == null) {
                            val overflow = systemActions.filter { it.kind(context) == null }
                            if (overflow.isNotEmpty()) {
                                Box {
                                    IconButton(onClick = { moreOpen = true }) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = stringResource(R.string.more_options),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    DropdownMenu(expanded = moreOpen, onDismissRequest = { moreOpen = false }) {
                                        overflow.forEach { action ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        action.title,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                },
                                                onClick = {
                                                    moreOpen = false
                                                    onSystemAction(action.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (showColors) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        BookmarkColorRow(
                            selected = mark?.color,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            onColor = { color ->
                                if (mark != null) {
                                    onEvent(ReaderEvent.OnEditBookmark(mark.id, color, mark.noteText.orEmpty()))
                                } else if (selection != null) {
                                    onEvent(
                                        ReaderEvent.OnAddBookmark(
                                            NewBookmark.Highlight(
                                                bookId = 0,
                                                chapterHref = "",
                                                quote = selection.quote,
                                                prefix = selection.prefix,
                                                suffix = selection.suffix,
                                                scrollPosition = selection.scrollPosition,
                                                color = color
                                            )
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun placeMenu(
    anchor: MenuAnchor,
    menuSize: IntSize,
    maxWidthPx: Int,
    maxHeightPx: Int,
    density: Float
): IntOffset {
    val width = menuSize.width.coerceAtLeast(1)
    val height = menuSize.height.coerceAtLeast(1)
    val hasAnchor = anchor.right > anchor.left || anchor.bottom > anchor.top
    if (!hasAnchor) {
        return IntOffset(((maxWidthPx - width) / 2f).roundToInt(), (24f * density).roundToInt())
    }
    val center = (anchor.left + anchor.right) / 2f * density
    val x = (center - width / 2f).roundToInt().coerceIn(8, (maxWidthPx - width - 8).coerceAtLeast(8))
    val top = anchor.top * density
    val bottom = anchor.bottom * density
    val rawY = if (top > height + 12f) top - height - 8f else bottom + 8f
    val y = rawY.roundToInt().coerceIn(8, (maxHeightPx - height - 8).coerceAtLeast(8))
    return IntOffset(x, y)
}

private enum class TextActionKind { COPY, SHARE, SELECT_ALL }

private fun SystemMenuAction.kind(context: android.content.Context): TextActionKind? {
    val value = title.trim().lowercase()
    fun same(resId: Int) = value == context.getString(resId).trim().lowercase()
    return when {
        same(android.R.string.copy) || value == "copy" || value == "copiar" -> TextActionKind.COPY
        same(android.R.string.selectAll) || value == "select all" || value == "seleccionar todo" ->
            TextActionKind.SELECT_ALL
        same(R.string.share) || value == "share" || value == "compartir" -> TextActionKind.SHARE
        else -> null
    }
}

private fun List<SystemMenuAction>.firstOfKind(
    kind: TextActionKind,
    context: android.content.Context
): SystemMenuAction? = firstOrNull { it.kind(context) == kind }

@Composable
private fun TextAction(title: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

private fun copyText(context: android.content.Context, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("bookmark", text))
}

private fun shareText(context: android.content.Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, null))
}
