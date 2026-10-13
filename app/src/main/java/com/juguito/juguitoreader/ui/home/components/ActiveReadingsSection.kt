package com.juguito.juguitoreader.ui.home.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Book

@Composable
fun ActiveReadingsSection(
    books: List<Book>,
    onOpenBook: (Book) -> Unit,
    onReadBook: (Int) -> Unit,
    onBookDetails: (Int) -> Unit,
    onDeleteBook: (Book) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (books.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeSectionHeader(
                title = stringResource(R.string.active_readings),
                iconImage = Icons.AutoMirrored.Outlined.MenuBook,
                badge = pluralStringResource(R.plurals.readings_in_progress, books.size, books.size),
                actionLabel = stringResource(R.string.see_all),
                onAction = onViewAll,
                actionChevron = true
            )
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val itemWidth = ((maxWidth - 32.dp - 25.dp) / 2).coerceIn(176.dp, 210.dp)
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                itemsIndexed(
                    items = books,
                    key = { _, book -> book.id },
                    contentType = { _, _ -> "active-reading" }
                ) { index, book ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (index > 0) {
                            VerticalDivider(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .height(90.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                            )
                        }
                        ActiveReadingItem(
                            book = book,
                            onOpen = { onOpenBook(book) },
                            onRead = { onReadBook(book.id) },
                            onDetails = { onBookDetails(book.id) },
                            onDelete = { onDeleteBook(book) },
                            modifier = Modifier.width(itemWidth)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveReadingItem(
    book: Book,
    onOpen: () -> Unit,
    onRead: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    val percentage = book.readingProgress?.percentage ?: 0
    val coverShape = RectangleShape

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onOpen,
                    onLongClick = { showMenu = true },
                    onLongClickLabel = stringResource(R.string.options),
                    role = Role.Button
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(90.dp)
                    .clip(coverShape)
            ) {
                BookCover(
                    coverUrl = book.coverUrl,
                    modifier = Modifier.fillMaxSize(),
                    shape = coverShape
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .padding(start = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = book.title,
                        style = homeSerif(size = 12.sp, lineHeight = 16.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = book.author,
                        modifier = Modifier.padding(top = 2.dp),
                        style = homeSans(size = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.progress_label),
                            modifier = Modifier.weight(1f),
                            style = homeSans(size = 10.sp, fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.progress_percent, percentage),
                            style = homeSans(size = 10.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                        drawStopIndicator = {}
                    )
                }
            }
        }

        BookHomeActionsMenu(
            expanded = showMenu,
            book = book,
            onDismiss = { showMenu = false },
            onRead = onRead,
            onDetails = onDetails,
            onDelete = onDelete
        )
    }
}
