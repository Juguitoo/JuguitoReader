package com.juguito.juguitoreader.ui.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
fun RegistryTab(
    state: BookDetailUiState.Success,
    onEvent: (BookDetailEvent) -> Unit,
    onShowStartDatePicker: () -> Unit,
    onShowEndDatePicker: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.reading_progress),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        BookStatusDropdown(
            selectedStatus = state.status,
            onStatusSelected = { onEvent(BookDetailEvent.OnStatusChanged(it)) }
        )

        RatingNumberInput(
            rating = state.rating ?: 0f,
            onRatingChanged = { onEvent(BookDetailEvent.OnRatingChanged(it)) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DatePickerField(
                label = stringResource(R.string.start_date_label),
                dateMillis = state.startDate,
                onClick = onShowStartDatePicker,
                modifier = Modifier.weight(1f)
            )
            DatePickerField(
                label = stringResource(R.string.end_date_label),
                dateMillis = state.endDate,
                onClick = onShowEndDatePicker,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = stringResource(R.string.notes_reflections),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = state.comment ?: "",
            onValueChange = { onEvent(BookDetailEvent.OnCommentChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            placeholder = { Text(stringResource(R.string.notes_placeholder)) },
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = { onEvent(BookDetailEvent.OnSaveClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isActionLoading,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            if (state.isActionLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.save_registry_changes))
            }
        }
    }
}