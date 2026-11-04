package com.juguito.juguitoreader.domain.usecase.bookmark

import com.juguito.juguitoreader.domain.model.Bookmark
import com.juguito.juguitoreader.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookBookmarksUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    operator fun invoke(bookId: Int): Flow<List<Bookmark>> {
        return repository.getBookBookmarks(bookId)
    }
}