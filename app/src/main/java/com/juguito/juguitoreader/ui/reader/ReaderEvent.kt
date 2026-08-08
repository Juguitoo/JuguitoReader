package com.juguito.juguitoreader.ui.reader

sealed interface ReaderEvent {
    data object OnNextChapter: ReaderEvent
    data object OnPreviousChapter: ReaderEvent
    data object OnToggleControls: ReaderEvent
    data class OnChapterSelected(val index: Int): ReaderEvent
}