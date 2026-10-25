package com.juguito.juguitoreader.ui.stats

import com.juguito.juguitoreader.domain.enums.StatsPeriod

sealed interface StatsEvent {
    data class OnSelectPeriod(val period: StatsPeriod) : StatsEvent
    data object OnPreviousPeriod : StatsEvent
    data object OnNextPeriod : StatsEvent
    data object OnRetry : StatsEvent
}