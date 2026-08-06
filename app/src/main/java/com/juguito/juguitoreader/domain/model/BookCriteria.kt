package com.juguito.juguitoreader.domain.model

data class BookCriteria(
    val searchText: String = "",
    val statuses: Set<BookStatus> = emptySet(),
    val series: String? = null,
    val sortBy: SortOption = SortOption.CREATED_AT_DESC
)

enum class SortOption(val displayName: String) {
    TITLE_ASC("Título (A-Z)"),
    TITLE_DESC("Título (Z-A)"),
    RATING_DESC("Mejor nota"),
    RATING_ASC("Peor nota"),
    CREATED_AT_DESC("Más reciente"),
    CREATED_AT_ASC("Más antiguo"),
    SERIES_ORDER_ASC("Orden en la saga")
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
            SortOption.CREATED_AT_DESC -> compareByDescending { it.createdAt }
            SortOption.CREATED_AT_ASC -> compareBy { it.createdAt }
            SortOption.SERIES_ORDER_ASC -> compareBy<Book> { it.series?.lowercase() }.thenBy { it.seriesOrder }
        }
    )
}
