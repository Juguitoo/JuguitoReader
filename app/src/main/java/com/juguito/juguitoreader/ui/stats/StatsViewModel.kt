package com.juguito.juguitoreader.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.StatsPeriod
import com.juguito.juguitoreader.domain.usecase.stats.ObserveReadingStatsUseCase
import com.juguito.juguitoreader.domain.usecase.stats.nextStatsAnchor
import com.juguito.juguitoreader.domain.usecase.stats.periodRange
import com.juguito.juguitoreader.domain.usecase.stats.previousStatsAnchor
import com.juguito.juguitoreader.ui.common.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val observeReadingStatsUseCase: ObserveReadingStatsUseCase,
) : ViewModel() {

    private val selection = MutableStateFlow(StatsSelection(StatsPeriod.MONTH, LocalDate.now()))
    private val retry = MutableStateFlow(0)

    val uiState: StateFlow<StatsUiState> = combine(selection, retry) { current, _ -> current }
        .flatMapLatest { current ->
            val today = LocalDate.now()
            val month = heatmapMonth(current.period, current.anchor, today)
            combine(
                observeReadingStatsUseCase(current.period, current.anchor),
                observeReadingStatsUseCase(StatsPeriod.MONTH, month.atDay(1)),
            ) { stats, heatmap ->
                StatsUiState.Success(
                    selectedPeriod = current.period,
                    anchor = current.anchor,
                    canGoForward = nextStatsAnchor(current.period, current.anchor, today) != null,
                    stats = stats,
                    heatmapMonth = month,
                    heatmapDays = heatmap.readingMinutes,
                ) as StatsUiState
            }
        }
        .catch {
            emit(StatsUiState.Error(UiText.StringResource(R.string.something_went_wrong)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState.Loading,
        )

    fun onEvent(event: StatsEvent) {
        when (event) {
            is StatsEvent.OnSelectPeriod -> {
                selection.value = StatsSelection(event.period, LocalDate.now())
            }
            StatsEvent.OnPreviousPeriod -> {
                val current = selection.value
                val previous = previousStatsAnchor(current.period, current.anchor) ?: return
                selection.value = current.copy(anchor = previous)
            }
            StatsEvent.OnNextPeriod -> {
                val current = selection.value
                val next = nextStatsAnchor(current.period, current.anchor, LocalDate.now()) ?: return
                selection.value = current.copy(anchor = next)
            }
            StatsEvent.OnRetry -> retry.value++
        }
    }

    private data class StatsSelection(
        val period: StatsPeriod,
        val anchor: LocalDate,
    )
}

internal fun heatmapMonth(period: StatsPeriod, anchor: LocalDate, today: LocalDate): YearMonth {
    return when (period) {
        StatsPeriod.WEEK -> YearMonth.from(periodRange(period, anchor)?.start ?: anchor)
        StatsPeriod.MONTH -> YearMonth.from(anchor)
        StatsPeriod.YEAR -> if (anchor.year == today.year) {
            YearMonth.from(today)
        } else {
            YearMonth.of(anchor.year, 12)
        }
    }
}
