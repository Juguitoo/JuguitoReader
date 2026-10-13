package com.juguito.juguitoreader.ui.home.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Book

@Composable
fun PendingBookRow(
    book: Book,
    onOpen: () -> Unit,
    onRead: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    val coverShape = RectangleShape

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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.height(90.dp))
                BookCover(
                    coverUrl = book.coverUrl,
                    modifier = Modifier.matchParentSize(),
                    shape = coverShape
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = book.title,
                            style = homeSerif(size = 14.sp, lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = book.author,
                            modifier = Modifier.padding(top = 2.dp),
                            style = homeSans(size = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                }
                PendingGenreLine(
                    names = book.genres.map { it.name },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
        )
    }
}

@Composable
private fun PendingGenreLine(
    names: List<String>,
    modifier: Modifier = Modifier,
) {
    val genres = names.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(3)
    if (genres.isEmpty()) return

    Text(
        text = genres.joinToString(" · "),
        modifier = modifier,
        style = homeSans(size = 12.sp).copy(fontStyle = FontStyle.Italic),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
