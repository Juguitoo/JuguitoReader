package com.juguito.juguitoreader.ui.book.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.ui.common.toUiText

@Composable
fun BookStatusChips(
    selectedStatus: BookStatus,
    onStatusSelected: (BookStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BookStatus.entries.forEach { status ->
            val icon = when (status) {
                BookStatus.PENDING -> Icons.Default.Schedule
                BookStatus.READING -> Icons.AutoMirrored.Filled.MenuBook
                BookStatus.FINISHED -> Icons.Default.CheckCircleOutline
                BookStatus.DROPPED -> Icons.Default.PauseCircleOutline
            }

            val tint = when (status) {
                BookStatus.PENDING -> Color(0xFFD9822B)
                BookStatus.READING -> MaterialTheme.colorScheme.primary
                BookStatus.FINISHED -> Color(0xFF4CAF50)
                BookStatus.DROPPED -> MaterialTheme.colorScheme.error
            }

            BookStatusChip(
                selected = status == selectedStatus,
                onClick = { onStatusSelected(status) },
                label =  status.toUiText().asString(),
                icon = icon,
                tint = tint
            )
        }
    }
}

@Composable
fun BookStatusChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector,
    tint: Color
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = animateColorAsState(if (selected) tint.copy(alpha = 0.18f) else Color.Transparent, label = "chipColor", animationSpec = tween(200)).value,
        border = BorderStroke(1.dp, color = if (selected) tint else tint.copy(alpha = 0f))
    ) {
        Row(
            modifier = Modifier.height(40.dp).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))

            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(200)) + expandHorizontally(tween(220), expandFrom = Alignment.Start),
                exit = fadeOut(tween(200)) + shrinkHorizontally(tween(220), shrinkTowards = Alignment.Start)
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
