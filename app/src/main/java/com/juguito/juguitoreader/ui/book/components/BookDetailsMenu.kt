package com.juguito.juguitoreader.ui.book.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.book.detail.BookDetailEvent

@Composable
fun BookDetailsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEvent: (BookDetailEvent) -> Unit
) {
    if (!expanded) return

    val density = LocalDensity.current
    val verticalOffsetPx = with(density) { 36.dp.roundToPx() }

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(x = 0, y = verticalOffsetPx),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 4.dp
        ) {
            Column (
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .padding(vertical = 4.dp)
            ) {

                BookDetailsMenuItem(
                    icon = Icons.Default.Edit,
                    text = stringResource(R.string.edit_book),
                    onClick = {
                        onDismiss()
                        onEvent(BookDetailEvent.OnEditModeChanged(true))
                    },
                    tint = MaterialTheme.colorScheme.primary
                )

                BookDetailsMenuItem(
                    icon = Icons.Default.Delete,
                    text = stringResource(R.string.delete),
                    onClick = {
                        onDismiss()
                        onDelete()
                    },
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun BookDetailsMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = text,
            modifier = Modifier.size(20.dp),
            tint = tint
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}