package com.juguito.juguitoreader.ui.reader

import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.EpubContent
import com.juguito.juguitoreader.domain.model.ReadingProgress

sealed interface ReaderUiState {
    data object Loading: ReaderUiState
    data class Error(val message: String): ReaderUiState
    data class Success(
        val book: Book,
        val epubContent: EpubContent,
        val readingProgress: ReadingProgress,
        val currentChapterIndex: Int,
        val isControlsVisible: Boolean = false
    ): ReaderUiState {
        val currentChapterUrl: String
            get() = "file://${epubContent.baseDir}/${epubContent.spine[currentChapterIndex]}"
    }
}
