package com.juguito.juguitoreader.domain.model

import androidx.annotation.StringRes
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus

data class BookCriteria(
    val searchText: String = "",
    val statuses: Set<BookStatus> = emptySet(),
    val series: String? = null,
    val sortBy: SortOption = SortOption.STARTED_AT_DESC
)

enum class SortOption(@StringRes val displayName: Int) {
    TITLE_ASC(R.string.sort_title_asc),
    TITLE_DESC(R.string.sort_title_desc),
    RATING_DESC(R.string.sort_rating_desc),
    RATING_ASC(R.string.sort_rating_asc),
    STARTED_AT_DESC(R.string.sort_started_at_desc),
    STARTED_AT_ASC(R.string.sort_started_at_asc),
    SERIES_ORDER_ASC(R.string.series_order)
}

fun List<Book>.applyCriteria(criteria: BookCriteria): List<Book> {
    return this.filter { book ->
        val matchesText = criteria.searchText.isBlank() || 
                book.title.contains(criteria.searchText, ignoreCase = true) ||
                book.author.contains(criteria.searchText, ignoreCase = true) ||
                (book.series?.contains(criteria.searchText, ignoreCase = true) ?: false)
        
        val matchesStatus = criteria.statuses.isEmpty() || criteria.statuses.contains(book.status)
        
        val matchesSeries = criteria.series == null || book.series == criteria.series
        
        matchesText && matchesStatus && matchesSeries
    }.sortedWith(
        when (criteria.sortBy) {
            SortOption.TITLE_ASC -> compareBy { it.title.lowercase() }
            SortOption.TITLE_DESC -> compareByDescending { it.title.lowercase() }
            SortOption.RATING_DESC -> compareByDescending { it.rating }
            SortOption.RATING_ASC -> compareBy { it.rating }
            SortOption.STARTED_AT_DESC -> compareByDescending { it.startDate }
            SortOption.STARTED_AT_ASC -> compareBy { it.startDate }
            SortOption.SERIES_ORDER_ASC -> compareBy<Book> { it.series?.lowercase() }.thenBy { it.seriesOrder }
        }
    )
}
