package com.juguito.juguitoreader.ui.library.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.ui.common.toUiText
import com.juguito.juguitoreader.ui.home.components.BookCover
import com.juguito.juguitoreader.ui.home.components.CoverSpineScrim

@Composable
fun BookLibraryRow(
    book: Book,
    onOpen: () -> Unit,
    onDetails: () -> Unit,
    onStatusChange: (BookStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .combinedClickable(
                    onClick = onOpen,
                    onLongClick = { showMenu = true },
                    onLongClickLabel = stringResource(R.string.options),
                    role = Role.Button
                )
                .padding(horizontal = 6.dp, vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxHeight()
                    .libraryCoverShadow()
            ) {
                Spacer(modifier = Modifier.height(96.dp / 0.7f))
                BookCover(
                    coverUrl = book.coverUrl,
                    modifier = Modifier.matchParentSize(),
                    shape = RectangleShape
                )
                CoverSpineScrim(modifier = Modifier.matchParentSize())
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (book.isFavourite) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = stringResource(R.string.favourite),
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(1f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = stringResource(R.string.options),
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                BookLibraryActionsMenu(
                                    expanded = showMenu,
                                    book = book,
                                    onDismiss = { showMenu = false },
                                    onDetails = onDetails,
                                    onStatusChange = onStatusChange
                                )
                            }
                        }
                    }
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontStyle = FontStyle.Italic
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                )

                LibraryMetaLine(book = book)
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 6.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
        )
    }
}

@Composable
private fun LibraryMetaLine(
    book: Book,
    modifier: Modifier = Modifier,
) {
    val statusColor = bookStatusColor(book.status)
    val percentage = if (book.status == BookStatus.READING && !book.isPhysical) {
        book.readingProgress?.percentage ?: 0
    } else {
        null
    }
    val statusText = book.status.toUiText().asString().let { label ->
        if (percentage != null) "$label · $percentage%" else label
    }
    val genres = book.genres.map { it.name.trim() }.filter { it.isNotEmpty() }.distinct().take(3)
    val detailColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column (
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = statusColor, fontWeight = FontWeight.Normal)) {
                    append(statusText)
                    if (book.isPhysical) {
                        append(" · ")
                        append(stringResource(R.string.physical_badge))
                    }
                }
            },
            modifier = modifier,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (genres.isEmpty()) return

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = detailColor, fontStyle = FontStyle.Italic)) {
                    append(genres.joinToString(" · "))
                }
            },
            modifier = modifier,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
