package com.juguito.juguitoreader.ui.registry

import com.juguito.juguitoreader.domain.model.BookCriteria
import com.juguito.juguitoreader.domain.model.BookStatus

sealed interface RegistryEvent{
    // Edición directa
    data class OnRatingChanged(val bookId: Int, val rating: Float): RegistryEvent
    data class OnCommentChanged(val bookId: Int, val comment: String): RegistryEvent
    data class OnStatusChanged(val bookId: Int, val status: BookStatus): RegistryEvent
    data class OnStartDateChanged(val bookId: Int, val startDate: Long?): RegistryEvent
    data class OnEndDateChanged(val bookId: Int, val endDate: Long?): RegistryEvent

    // Filtros y Búsqueda
    data class OnSearchTextChanged(val text: String): RegistryEvent
    data class OnToggleSearch(val expanded: Boolean): RegistryEvent
    data class OnShowFilterSheet(val show: Boolean): RegistryEvent
    data class OnCriteriaChanged(val criteria: BookCriteria): RegistryEvent
    data object OnClearFilters: RegistryEvent
}
