package com.juguito.juguitoreader.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Book

@Composable
fun BookHomeActionsMenu(
    expanded: Boolean,
    book: Book,
    onDismiss: () -> Unit,
    onRead: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
    showReadAction: Boolean = true
) {
    if (!expanded) return

    val canRead = showReadAction && !book.isPhysical && !book.localFilePath.isNullOrBlank()
    val density = LocalDensity.current
    val gapPx = with(density) { 4.dp.roundToPx() }
    val marginPx = with(density) { 8.dp.roundToPx() }
    val positionProvider = remember(gapPx, marginPx) {
        EndAlignedMenuPositionProvider(gapPx, marginPx)
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .height(44.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (canRead) {
                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = { onDismiss(); onRead() }
                    ) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = stringResource(R.string.read_book),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = { onDismiss(); onDetails() }
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.details)
                    )
                }

                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = { onDismiss(); onDelete() }
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private class EndAlignedMenuPositionProvider(
    private val verticalGapPx: Int,
    private val marginPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val preferredX = if (layoutDirection == LayoutDirection.Ltr) {
            anchorBounds.right - popupContentSize.width
        } else {
            anchorBounds.left
        }
        val maxX = (windowSize.width - popupContentSize.width - marginPx).coerceAtLeast(marginPx)
        val x = preferredX.coerceIn(marginPx, maxX)

        val below = anchorBounds.bottom + verticalGapPx
        val above = anchorBounds.top - popupContentSize.height - verticalGapPx
        val maxY = (windowSize.height - popupContentSize.height - marginPx).coerceAtLeast(marginPx)
        val y = when {
            below + popupContentSize.height <= windowSize.height - marginPx -> below
            above >= marginPx -> above
            else -> below.coerceIn(marginPx, maxY)
        }
        return IntOffset(x, y)
    }
}