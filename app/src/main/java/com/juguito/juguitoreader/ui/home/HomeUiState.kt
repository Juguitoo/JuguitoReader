package com.juguito.juguitoreader.ui.home

import com.juguito.juguitoreader.domain.model.Book

sealed interface HomeUiState {
    data object Loading: HomeUiState
    data object Empty: HomeUiState
    data class Error(val message: String): HomeUiState
    data class Success(
        val readingBooks: List<Book>,
        val pendingBooks: List<Book>,
        val stats: StatsUiState
    ): HomeUiState
}

data class StatsUiState(
    val totalBooksCount: Int = 0,
    val readingBooksCount: Int = 0,
    val finishedBooksCount: Int = 0,
    val readingVelocity: Int = 0,
    val totalReadingMinutes: Int = 0
)
