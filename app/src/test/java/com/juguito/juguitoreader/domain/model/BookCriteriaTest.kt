package com.juguito.juguitoreader.domain.model

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.enums.BookStatus
import org.junit.Test

class BookCriteriaTest {

    @Test
    fun `applyCriteria filters and keeps the original order`() {
        val books = listOf(
            Book(id = 1, title = "Later", author = "A", isPhysical = false, status = BookStatus.READING, startDate = 1),
            Book(id = 2, title = "Pending", author = "A", isPhysical = false, status = BookStatus.PENDING, startDate = 50),
            Book(id = 3, title = "Now", author = "A", isPhysical = false, status = BookStatus.READING, startDate = 20),
        )

        val filtered = books.applyCriteria(BookCriteria(statuses = setOf(BookStatus.READING)))

        assertThat(filtered.map { it.id }).containsExactly(1, 3).inOrder()
    }

    @Test
    fun `applyOrderBy sorts by start date descending`() {
        val books = listOf(
            Book(id = 1, title = "Later", author = "A", isPhysical = false, startDate = 1),
            Book(id = 2, title = "Pending", author = "A", isPhysical = false, startDate = 50),
            Book(id = 3, title = "Now", author = "A", isPhysical = false, startDate = 20),
        )

        val ordered = books.applyOrderBy(SortOption.STARTED_AT_DESC)

        assertThat(ordered.map { it.id }).containsExactly(2, 3, 1).inOrder()
    }
}
