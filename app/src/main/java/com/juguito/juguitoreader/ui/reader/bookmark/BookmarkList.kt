package com.juguito.juguitoreader.ui.reader.bookmark

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookmarkKind
import com.juguito.juguitoreader.domain.model.Bookmark
import com.juguito.juguitoreader.ui.theme.JuguitoPrimary
import com.juguito.juguitoreader.ui.theme.LoraFontFamily

@Composable
internal fun BookmarkSearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.4f
    val fieldColor = if (dark) Color(0xFF2C2622) else Color(0xFFF5EFE6)
    val borderColor = if (dark) Color(0xFF6B625A) else Color(0xFFD9CFC3)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 24.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { inner ->
            val hint = if (dark) Color(0xFFA1988F) else Color(0xFF756D65)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(fieldColor)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = hint
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.bookmark_search),
                            style = MaterialTheme.typography.bodySmall,
                            color = hint
                        )
                    }
                    inner()
                }
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear_search),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onQueryChange("") }
                            ),
                        tint = hint
                    )
                }
            }
        }
    )
}

private data class KindTabBounds(val x: Float, val width: Float, val height: Float)

private const val FILTER_ALL = "ALL"

@Composable
internal fun BookmarkKindSelector(
    selected: BookmarkKind?,
    total: Int,
    counts: Map<BookmarkKind, Int>,
    onSelect: (BookmarkKind?) -> Unit
) {
    val options = listOf<BookmarkKind?>(
        null,
        BookmarkKind.POSITION,
        BookmarkKind.HIGHLIGHT,
        BookmarkKind.NOTE
    )
    val density = LocalDensity.current
    var bounds by remember { mutableStateOf(mapOf<String, KindTabBounds>()) }
    val current = bounds[selected.tabKey()]
    val indicatorX by animateDpAsState(
        targetValue = with(density) { (current?.x ?: 0f).toDp() },
        animationSpec = tween(220),
        label = "bookmarkFilterX"
    )
    val indicatorWidth by animateDpAsState(
        targetValue = with(density) { (current?.width ?: 0f).toDp() },
        animationSpec = tween(220),
        label = "bookmarkFilterWidth"
    )
    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        if (current != null) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorX)
                    .width(indicatorWidth)
                    .height(with(density) { current.height.toDp() })
                    .clip(RoundedCornerShape(50))
                    .background(JuguitoPrimary)
            )
        }
        Row {
            options.forEach { kind ->
                val key = kind.tabKey()
                val active = selected == kind
                val count = if (kind == null) total else counts[kind] ?: 0
                val label = if (kind == null) {
                    stringResource(R.string.bookmark_filter_all)
                } else {
                    stringResource(kind.labelRes())
                }
                Text(
                    text = "$label $count",
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            val next = KindTabBounds(
                                x = coordinates.positionInParent().x,
                                width = coordinates.size.width.toFloat(),
                                height = coordinates.size.height.toFloat()
                            )
                            if (bounds[key] != next) bounds = bounds + (key to next)
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(kind) }
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun BookmarkKind?.tabKey(): String = this?.name ?: FILTER_ALL

@Composable
internal fun BookmarkRow(
    mark: Bookmark,
    chapterLabel: String?,
    canJump: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember(mark.id) { mutableStateOf(false) }
    val accent = mark.color?.swatch() ?: MaterialTheme.colorScheme.outline
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canJump, onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mark.kind == BookmarkKind.POSITION) {
                    Icon(
                        Icons.Default.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = JuguitoPrimary
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                }
                Text(
                    text = formatBookmarkTime(mark.createdAt),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (chapterLabel != null) {
                    Text(
                        text = "·",
                        modifier = Modifier.padding(horizontal = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = chapterLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.bookmark_options),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (mark.kind != BookmarkKind.POSITION) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit_bookmark_note)) },
                                onClick = {
                                    menuOpen = false
                                    onEdit()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            }
                        )
                    }
                }
            }
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent)
                )
                Text(
                    text = mark.displayQuote(),
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = LoraFontFamily,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!mark.noteText.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .size(14.dp),
                        tint = JuguitoPrimary
                    )
                    Text(
                        text = mark.noteText,
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
