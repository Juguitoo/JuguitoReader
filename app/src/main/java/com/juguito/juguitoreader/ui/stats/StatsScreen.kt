package com.juguito.juguitoreader.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.common.components.JuguitoTopBar
import com.juguito.juguitoreader.ui.components.ErrorView
import com.juguito.juguitoreader.ui.stats.components.FinishedBooksRow
import com.juguito.juguitoreader.ui.stats.components.StatsHeatmap
import com.juguito.juguitoreader.ui.stats.components.StatsInsights
import com.juguito.juguitoreader.ui.stats.components.StatsMetricGrid
import com.juguito.juguitoreader.ui.stats.components.StatsMinutesChart
import com.juguito.juguitoreader.ui.stats.components.StatsPeriodPager
import com.juguito.juguitoreader.ui.stats.components.StatsPeriodSelector
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import java.time.LocalDate

@Composable
fun StatsScreen(
    onNavigateToBookDetail: (Int) -> Unit,
    onViewFinishedBooks: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    StatsContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToBookDetail = onNavigateToBookDetail,
        onViewFinishedBooks = onViewFinishedBooks,
        modifier = modifier,
    )
}

@Composable
fun StatsContent(
    state: StatsUiState,
    onEvent: (StatsEvent) -> Unit,
    onNavigateToBookDetail: (Int) -> Unit,
    onViewFinishedBooks: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            JuguitoTopBar(
                title = {
                    Text(
                        text = stringResource(R.string.stats),
                        fontFamily = LoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (state) {
                StatsUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is StatsUiState.Error -> ErrorView(
                    message = state.message.asString(),
                    onRetry = { onEvent(StatsEvent.OnRetry) },
                )
                StatsUiState.Empty -> ErrorView(
                    message = stringResource(R.string.stats_time_from_reader),
                    onRetry = { onEvent(StatsEvent.OnRetry) },
                    title = stringResource(R.string.stats),
                )
                is StatsUiState.Success -> StatsSuccess(
                    state = state,
                    onEvent = onEvent,
                    onNavigateToBookDetail = onNavigateToBookDetail,
                    onViewFinishedBooks = onViewFinishedBooks,
                )
            }
        }
    }
}

@Composable
private fun StatsSuccess(
    state: StatsUiState.Success,
    onEvent: (StatsEvent) -> Unit,
    onNavigateToBookDetail: (Int) -> Unit,
    onViewFinishedBooks: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .widthIn(max = 640.dp)
            .fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.stats_kicker).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = stringResource(R.string.stats_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            StatsPeriodSelector(
                selected = state.selectedPeriod,
                onSelect = { onEvent(StatsEvent.OnSelectPeriod(it)) },
            )
        }
        item {
            StatsPeriodPager(
                period = state.selectedPeriod,
                anchor = state.anchor,
                canGoForward = state.canGoForward,
                onPrevious = { onEvent(StatsEvent.OnPreviousPeriod) },
                onNext = { onEvent(StatsEvent.OnNextPeriod) },
            )
        }
        item { StatsMetricGrid(stats = state.stats) }
        item {
            StatsMinutesChart(
                period = state.selectedPeriod,
                buckets = state.stats.readingMinutes,
                totalMinutes = state.stats.readingTimeComparison.currentMinutes,
            )
        }
        item {
            StatsHeatmap(
                month = state.heatmapMonth,
                weights = state.heatmapDays,
                today = LocalDate.now(),
                streak = state.stats.readingStreak,
                bestStreak = state.stats.bestReadingStreak,
            )
        }
        item {
            FinishedBooksRow(
                books = state.stats.finishedInPeriod,
                onBookClick = onNavigateToBookDetail,
                onViewAll = onViewFinishedBooks,
            )
        }
        item {
            StatsInsights(stats = state.stats)
        }
        if (state.stats.readingTimeComparison.currentMinutes == 0) {
            item {
                Text(
                    text = stringResource(R.string.stats_time_from_reader),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
