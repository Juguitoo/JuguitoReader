package com.juguito.juguitoreader.ui.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.ui.components.JuguitoDialog
import com.juguito.juguitoreader.ui.reader.ReaderEvent
import com.juguito.juguitoreader.ui.reader.ReaderUiState
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingSessionsSheet(
    state: ReaderUiState.Success,
    onEvent: (ReaderEvent) -> Unit
) {
    val sessions = remember(state.bookSessions) {
        state.bookSessions.sortedByDescending { it.date }
    }
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var pendingDeleteDate by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = { onEvent(ReaderEvent.OnToggleSessionsDialog) },
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.your_reading_sessions),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (sessions.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            isEditing = !isEditing
                            if (!isEditing) pendingDeleteDate = null
                        }
                    ) {
                        Text(
                            text = stringResource(if (isEditing) R.string.done else R.string.edit)
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.daily_progress_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (sessions.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_sessions_saved),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions, key = { it.date }) { session ->
                        SessionRow(
                            session = session,
                            isEditing = isEditing,
                            onDelete = { pendingDeleteDate = session.date }
                        )
                    }
                }
            }
        }
    }

    pendingDeleteDate?.let { date ->
        JuguitoDialog(
            onDismissRequest = { pendingDeleteDate = null },
            title = stringResource(R.string.delete_daily_reading_title),
            message = stringResource(R.string.delete_daily_reading_confirmation, date),
            confirmButtonText = stringResource(R.string.delete),
            dismissButtonText = stringResource(R.string.cancel),
            isDestructive = true,
            onConfirm = {
                onEvent(ReaderEvent.OnDeleteDailyReading(state.book.id, date))
                pendingDeleteDate = null
            }
        )
    }
}

@Composable
private fun SessionRow(
    session: DailyReading,
    isEditing: Boolean,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = if (isEditing) 0.dp else 12.dp)
                .padding(vertical = if (isEditing) 0.dp else 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = session.date,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SessionMetrics(session)
                if (isEditing) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(
                                R.string.delete_daily_reading_cd,
                                session.date
                            ),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionMetrics(session: DailyReading) {
    val minutes = ceil(session.timeSpentMillis / 60000.0).toInt()
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    val duration = when {
        hours == 0 -> stringResource(R.string.session_duration_minutes, minutes)
        remainingMinutes == 0 -> stringResource(R.string.session_duration_hours, hours)
        else -> stringResource(R.string.session_duration_hours_minutes, hours, remainingMinutes)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = duration,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.reading_speed, session.readingSpeed),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "${session.reachedPercentage}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
