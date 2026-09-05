package com.juguito.juguitoreader.ui.book.detail

import android.net.Uri
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre

sealed interface BookDetailEvent {
    data class OnTitleChanged(val title: String): BookDetailEvent
    data class OnAuthorChanged(val author: String): BookDetailEvent
    data class OnPublisherChanged(val publisher: String): BookDetailEvent
    data class OnSeriesChanged(val series: String): BookDetailEvent
    data class OnSeriesOrderChanged(val seriesOrder: String): BookDetailEvent
    data class OnIsPhysicalChanged(val isPhysical: Boolean): BookDetailEvent
    data class OnCoverUrlChanged(val coverUrl: String): BookDetailEvent
    data class OnLocalFilePathChanged(val localFilePath: String?): BookDetailEvent
    data class OnEpubFilePicked(val uri: Uri): BookDetailEvent
    data class OnFoldersChanged(val folders: List<Folder>): BookDetailEvent
    data class OnGenresChanged(val genres: List<Genre>): BookDetailEvent
    data class OnStartDateChanged(val startDate: Long?): BookDetailEvent
    data class OnEndDateChanged(val endDate: Long?): BookDetailEvent
    data class OnRatingChanged(val rating: Float): BookDetailEvent
    data class OnCommentChanged(val comment: String): BookDetailEvent
    data class OnStatusChanged(val status: BookStatus): BookDetailEvent
    data class OnEditModeChanged(val mode: Boolean): BookDetailEvent
    data class OnTabChanged(val tab: Int): BookDetailEvent
    data object OnSaveClick: BookDetailEvent
    data object OnDeleteClick: BookDetailEvent
    data object OnDiscard: BookDetailEvent
}
