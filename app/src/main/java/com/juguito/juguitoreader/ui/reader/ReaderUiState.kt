package com.juguito.juguitoreader.ui.reader

import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Bookmark
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.model.EpubContent
import com.juguito.juguitoreader.domain.model.ReadingProgress

import com.juguito.juguitoreader.ui.common.UiText

enum class ReaderTheme(val bgColor: String, val textColor: String) {
    DAY(bgColor = "#FFFFFF", textColor = "#1A1A1A"),
    SEPIA(bgColor = "#EEE7D7", textColor = "#1A1A1A"),
    NIGHT(bgColor = "#000000", textColor = "#CCCCCC")
}

enum class LibraryTab { BOOKMARKS, IMAGES }

sealed interface ReaderUiState {
    data object Loading: ReaderUiState
    data class Error(val message: UiText): ReaderUiState
    data class Success(
        val book: Book,
        val epubContent: EpubContent,
        val readingProgress: ReadingProgress,
        val bookSessions: List<DailyReading>,
        val bookmarks: List<Bookmark>,
        val currentChapterIndex: Int,
        val isControlsVisible: Boolean = false,
        val textZoom: Int = 100,
        val theme: ReaderTheme = ReaderTheme.SEPIA,
        val brightness: Float = 0.5f,
        val timeRemaining: Int? = null,
        val showStatusPrompt: Boolean = false,
        val showSessionsDialog: Boolean = false,
        val showBookmarkList: Boolean = false,
        val selectedBookmark: Bookmark? = null,
        val pendingJump: Bookmark? = null,
        val pendingSelection: TextSelection? = null,
        val showNewNote: Boolean = false,
        val markMenuId: Int? = null,
        val markMenuAnchor: MenuAnchor? = null,
        val captureBookmarkRequest: Int = 0,
        val clearSelectionRequest: Int = 0,
        val lastReportedChapterWords: Int = 0,
        val accumulatedReadWords: Int = 0,
        val webViewInstanceKey: Int = 0,
        val libraryTab: LibraryTab = LibraryTab.BOOKMARKS,
        val openImageId: Int? = null
    ): ReaderUiState {
        val currentChapterUrl: String
            get() = epubChapterUrl(epubContent.spine[currentChapterIndex])
    }
}
