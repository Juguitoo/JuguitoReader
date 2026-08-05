package com.juguito.juguitoreader.ui.home

data class StatsUiState(
    val totalBooks: Int = 0,
    val readingBooks: Int = 0,
    val finishedBooks: Int = 0,
    val readingVelocity: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)