package com.juguito.juguitoreader.ui.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.graphics.toColorInt
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.reader.ReaderUiState

@Composable
fun ReaderFooter(
    state: ReaderUiState.Success,
    modifier: Modifier
) {
    AnimatedVisibility(
        visible = !state.isControlsVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val footerColor = Color(state.theme.textColor.toColorInt()).copy(alpha = 0.4f)

            Text(
                text = "${state.currentChapterIndex + 1} / ${state.epubContent.spine.size}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = footerColor
            )

            if (state.timeRemaining != null) {
                val timeText = if (state.timeRemaining == 0) stringResource(R.string.less_than_one_min) else stringResource(R.string.minutes_remaining, state.timeRemaining)
                Text(
                    text = stringResource(R.string.time_remaining_label, timeText),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = footerColor
                )
            }
        }
    }
}