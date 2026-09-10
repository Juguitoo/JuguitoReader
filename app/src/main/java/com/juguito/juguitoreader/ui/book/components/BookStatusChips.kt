package com.juguito.juguitoreader.ui.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BookStatus.entries.forEach { status ->
            FilterChip(
                selected = status == selectedStatus,
                onClick = { onStatusSelected(status) },
                label = { Text(status.toUiText().asString()) }
            )
        }
    }
}
