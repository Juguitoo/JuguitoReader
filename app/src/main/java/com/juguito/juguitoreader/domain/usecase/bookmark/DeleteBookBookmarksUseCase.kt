package com.juguito.juguitoreader.domain.usecase.bookmark

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.repository.BookmarkRepository
import javax.inject.Inject

class DeleteBookBookmarksUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> {
        return try {
            repository.deleteBookBookmarks(bookId)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(JuguitoException(R.string.error_delete_bookmarks))
        }
    }
}