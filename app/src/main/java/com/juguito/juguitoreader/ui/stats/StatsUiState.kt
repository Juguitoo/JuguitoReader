package com.juguito.juguitoreader.ui.stats

import com.juguito.juguitoreader.domain.enums.StatsPeriod
import com.juguito.juguitoreader.domain.model.ReadingStats
import com.juguito.juguitoreader.domain.model.ReadingStatsBucket
import com.juguito.juguitoreader.ui.common.UiText
import java.time.LocalDate
import java.time.YearMonth

sealed interface StatsUiState {
    data object Loading : StatsUiState
    data object Empty : StatsUiState
    data class Error(val message: UiText) : StatsUiState
    data class Success(
        val selectedPeriod: StatsPeriod,
        val anchor: LocalDate,
        val canGoForward: Boolean,
        val stats: ReadingStats,
        val heatmapMonth: YearMonth,
        val heatmapDays: List<ReadingStatsBucket>,
    ) : StatsUiState
}
