package com.juguito.juguitoreader.ui.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.ui.common.toUiText

@Composable
fun StatusBadge(status: BookStatus, percentage: Int?, modifier: Modifier = Modifier) {
    if (status == BookStatus.PENDING) return

    val color = when (status) {
        BookStatus.READING -> MaterialTheme.colorScheme.primary
        BookStatus.FINISHED -> Color(0xFF4CAF50)
        BookStatus.DROPPED -> MaterialTheme.colorScheme.error
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.95f),
        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(4.dp, 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = status.toUiText().asString().uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )

            if (status == BookStatus.READING && percentage != null) {
                Text(
                    text = "${percentage}%",
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}