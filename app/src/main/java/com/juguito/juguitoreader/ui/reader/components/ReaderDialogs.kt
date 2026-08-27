package com.juguito.juguitoreader.ui.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.components.JuguitoDialog
import com.juguito.juguitoreader.ui.reader.ReaderEvent
import com.juguito.juguitoreader.ui.reader.ReaderUiState
import kotlin.math.ceil

@Composable
fun ReadingSessionsDialog(
    state: ReaderUiState.Success,
    onEvent: (ReaderEvent) -> Unit
) {
    JuguitoDialog(
        onDismissRequest = { onEvent(ReaderEvent.OnToggleSessionsDialog) },
        icon = Icons.Default.BarChart,
        title = stringResource(R.string.your_reading_sessions),
        message = stringResource(R.string.daily_progress_message),
        dismissButtonText = stringResource(R.string.close),
        content = {
            if (state.bookSessions.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_sessions_saved),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp).padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.bookSessions.sortedByDescending { it.date }) { session ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = session.date,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val minutes = ceil(session.timeSpentMillis / 60000.0).toInt()
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(
                                        text = stringResource(R.string.minutes_remaining, minutes),
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
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ChangeStatusPromptDialog(onEvent: (ReaderEvent) -> Unit) {
    JuguitoDialog(
        onDismissRequest = { onEvent(ReaderEvent.OnStatusPromptResult(false)) },
        icon = Icons.Default.AutoStories,
        title = stringResource(R.string.change_status_prompt_title),
        message = stringResource(R.string.change_status_prompt_message),
        confirmButtonText = stringResource(R.string.yes_change),
        dismissButtonText = stringResource(R.string.keep_same),
        onConfirm = { onEvent(ReaderEvent.OnStatusPromptResult(true)) }
    )
}