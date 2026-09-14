package com.juguito.juguitoreader.ui.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.book.detail.BookDetailEvent
import com.juguito.juguitoreader.ui.book.detail.BookDetailUiState

@Composable
fun ReadingJournal(
    state: BookDetailUiState.Success,
    onEvent: (BookDetailEvent) -> Unit,
    onShowStartDatePicker: () -> Unit,
    onShowEndDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.reading_progress),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        BookStatusChips(
            selectedStatus = state.status,
            onStatusSelected = { onEvent(BookDetailEvent.OnStatusChanged(it)) }
        )

        RatingNumberInput(
            rating = state.rating ?: 0f,
            onRatingChanged = { onEvent(BookDetailEvent.OnRatingChanged(it)) }
        )

        JournalDateRange(
            startMillis = state.startDate,
            endMillis = state.endDate,
            onStartClick = onShowStartDatePicker,
            onEndClick = onShowEndDatePicker
        )

        OutlinedTextField(
            value = state.comment ?: "",
            onValueChange = { onEvent(BookDetailEvent.OnCommentChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp),
            placeholder = { Text(stringResource(R.string.notes_placeholder)) },
            supportingText = { Text(stringResource(R.string.notes_autosave)) },
            shape = RoundedCornerShape(12.dp)
        )
    }
}
