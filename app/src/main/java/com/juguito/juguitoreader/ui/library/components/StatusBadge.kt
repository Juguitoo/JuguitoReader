package com.juguito.juguitoreader.ui.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

@Composable
fun StatusBadge(status: BookStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        BookStatus.READING -> MaterialTheme.colorScheme.primary
        BookStatus.FINISHED -> Color(0xFF4CAF50)
        BookStatus.DROPPED -> MaterialTheme.colorScheme.error
        BookStatus.PENDING -> return
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp),
        color = color.copy(alpha = 0.85f),
        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = status.displayName.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}