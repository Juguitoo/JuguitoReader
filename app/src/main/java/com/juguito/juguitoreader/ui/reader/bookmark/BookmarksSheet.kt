package com.juguito.juguitoreader.ui.reader.bookmark

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookmarkKind
import com.juguito.juguitoreader.domain.model.Bookmark
import com.juguito.juguitoreader.ui.reader.LibraryTab
import com.juguito.juguitoreader.ui.reader.ReaderEvent
import com.juguito.juguitoreader.ui.reader.ReaderUiState
import com.juguito.juguitoreader.ui.theme.JuguitoPrimary
import com.juguito.juguitoreader.ui.theme.LoraFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksSheet(
    state: ReaderUiState.Success,
    onEvent: (ReaderEvent) -> Unit
) {
    val spineOrder = remember(state.epubContent.spine) {
        state.epubContent.spine.withIndex().associate { it.value to it.index }
    }
    val marks = remember(state.bookmarks, spineOrder) {
        state.bookmarks
            .filter { it.kind != BookmarkKind.IMAGE }
            .sortedWith(
                compareBy<Bookmark> { spineOrder[it.chapterHref] ?: Int.MAX_VALUE }
                    .thenBy { it.scrollPosition }
            )
    }
    var query by remember { mutableStateOf("") }
    var selectedKind by remember { mutableStateOf<BookmarkKind?>(null) }
    val visible = remember(marks, query, selectedKind) {
        val needle = query.trim().forSearch()
        marks.filter { mark ->
            (selectedKind == null || mark.kind == selectedKind) && mark.matchesQuery(needle)
        }
    }

    val counts = remember(marks) { marks.groupingBy { it.kind }.eachCount() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(ReaderEvent.OnCloseLibrarySheet) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(modifier = Modifier.fillMaxWidth()) {
                BottomSheetDefaults.DragHandle(modifier = Modifier.align(Alignment.Center))
                IconButton(
                    onClick = { onEvent(ReaderEvent.OnCloseLibrarySheet) },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Text(
                        text = stringResource(R.string.bookmarks_and_notes),
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = LoraFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(
                            R.string.bookmarks_sheet_subtitle,
                            state.book.title,
                            pluralStringResource(R.plurals.bookmarks_saved, marks.size, marks.size)
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                LibraryTabs(
                    selected = state.libraryTab,
                    onSelect = { onEvent(ReaderEvent.OnSelectLibraryTab(it)) }
                )
                if (state.libraryTab == LibraryTab.BOOKMARKS) {
                    if (marks.isNotEmpty()) {
                        BookmarkSearchField(
                            query = query,
                            onQueryChange = { query = it }
                        )
                        BookmarkKindSelector(
                            selected = selectedKind,
                            total = marks.size,
                            counts = counts,
                            onSelect = { selectedKind = it }
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp)) {
                        when {
                            marks.isEmpty() -> {
                                Text(
                                    text = stringResource(R.string.bookmarks_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            visible.isEmpty() -> {
                                Text(
                                    text = stringResource(R.string.bookmarks_no_matches),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    itemsIndexed(visible, key = { _, mark -> mark.id }) { _, mark ->
                                        val chapter = spineOrder[mark.chapterHref]?.plus(1)
                                        BookmarkRow(
                                            mark = mark,
                                            chapterLabel = chapter?.let {
                                                stringResource(R.string.bookmark_chapter, it)
                                            },
                                            canJump = spineOrder.containsKey(mark.chapterHref),
                                            onOpen = { onEvent(ReaderEvent.OnMoveToBookmark(mark)) },
                                            onEdit = { onEvent(ReaderEvent.OnOpenNote(mark.id)) },
                                            onDelete = { onEvent(ReaderEvent.OnDeleteBookmark(mark.id)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    PinnedImagePage(state = state, onEvent = onEvent)
                }
            }
            if (state.openImageId != null) {
                PinnedImageViewer(
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun LibraryTabs(
    selected: LibraryTab,
    onSelect: (LibraryTab) -> Unit
) {
    val tabs = listOf(LibraryTab.BOOKMARKS, LibraryTab.IMAGES)
    var width by remember { mutableStateOf(0) }
    var trackHeight by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val index = tabs.indexOf(selected).coerceAtLeast(0)
    val indicatorX by animateDpAsState(
        targetValue = with(density) { (width / tabs.size * index).toDp() },
        animationSpec = tween(220),
        label = "libraryTabX"
    )
    val track = if (MaterialTheme.colorScheme.background.luminance() < 0.4f) {
        Color(0xFF2C2622)
    } else {
        Color(0xFFF5EFE6)
    }
    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(track)
            .onGloballyPositioned { width = it.size.width }
    ) {
        if (width > 0 && trackHeight > 0) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorX)
                    .width(with(density) { (width / tabs.size).toDp() })
                    .height(with(density) { trackHeight.toDp() })
                    .padding(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(JuguitoPrimary)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { trackHeight = it.size.height }
        ) {
            tabs.forEach { tab ->
                val label = if (tab == LibraryTab.BOOKMARKS) {
                    stringResource(R.string.bookmarks)
                } else {
                    stringResource(R.string.bookmark_kind_image)
                }
                Text(
                    text = label,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(tab) }
                        )
                        .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected == tab) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected == tab) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
