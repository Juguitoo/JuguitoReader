package com.juguito.juguitoreader.ui.book.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun JournalDateRange(
    startMillis: Long?,
    endMillis: Long?,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val startPlaceholder = stringResource(R.string.add_start_date)
    val endPlaceholder = stringResource(R.string.add_end_date)
    val startText = remember(startMillis, startPlaceholder) {
        formatJournalDate(startMillis) ?: startPlaceholder
    }
    val endText = remember(endMillis, endPlaceholder) {
        formatJournalDate(endMillis) ?: endPlaceholder
    }
    val dateStyle = MaterialTheme.typography.bodyLarge

    Row(
        modifier = modifier.heightIn(min = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateSegment(
            text = startText,
            isPlaceholder = startMillis == null,
            contentDescription = stringResource(R.string.start_date_label) + ", " + startText,
            onClick = onStartClick,
            style = dateStyle,
            modifier = Modifier.alignBy(LastBaseline)
        )
        Text(
            text = stringResource(R.string.date_range_separator),
            style = dateStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .alignBy(LastBaseline)
        )
        DateSegment(
            text = endText,
            isPlaceholder = endMillis == null,
            contentDescription = stringResource(R.string.end_date_label) + ", " + endText,
            onClick = onEndClick,
            style = dateStyle,
            modifier = Modifier.alignBy(LastBaseline)
        )
    }
}

@Composable
private fun DateSegment(
    text: String,
    isPlaceholder: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp)
            .semantics { this.contentDescription = contentDescription },
        style = style,
        color = if (isPlaceholder) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    )
}

internal fun formatJournalDate(millis: Long?): String? {
    if (millis == null) return null
    val format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(Date(millis))
}
