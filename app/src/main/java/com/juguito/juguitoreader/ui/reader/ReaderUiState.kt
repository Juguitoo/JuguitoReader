package com.juguito.juguitoreader.ui.reader

import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.model.EpubContent
import com.juguito.juguitoreader.domain.model.ReadingProgress

enum class ReaderTheme(val bgColor: String, val textColor: String) {
    DAY(bgColor = "#FFFFFF", textColor = "#1A1A1A"),
    SEPIA(bgColor = "#EEE7D7", textColor = "#1A1A1A"),
    NIGHT(bgColor = "#000000", textColor = "#CCCCCC")
}

sealed interface ReaderUiState {
    data object Loading: ReaderUiState
    data class Error(val message: String): ReaderUiState
    data class Success(
        val book: Book,
        val epubContent: EpubContent,
        val readingProgress: ReadingProgress,
        val bookSessions: List<DailyReading>,
        val currentChapterIndex: Int,
        val isControlsVisible: Boolean = false,
        val textZoom: Int = 100,
        val theme: ReaderTheme = ReaderTheme.SEPIA,
        val brightness: Float = 0.5f,
        val timeRemaining: Int? = null,
        val showStatusPrompt: Boolean = false,
        val showSessionsDialog: Boolean = false
    ): ReaderUiState {
        val currentChapterUrl: String
            get() = "file://${epubContent.baseDir}/${epubContent.spine[currentChapterIndex]}"
    }
}
