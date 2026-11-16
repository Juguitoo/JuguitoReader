package com.juguito.juguitoreader.domain.usecase.bookmark

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookmarkKind
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Bookmark
import com.juguito.juguitoreader.domain.model.copy
import com.juguito.juguitoreader.domain.repository.BookmarkRepository
import javax.inject.Inject

class UpdateBookmarkUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    suspend operator fun invoke(bookmark: Bookmark): Result<Bookmark> {
        if (bookmark.kind == BookmarkKind.IMAGE) {
            val noteText = bookmark.noteText?.trim()?.ifBlank { null }
            if (noteText != null && noteText.length > MAX_BOOKMARK_NOTE) {
                return Result.failure(JuguitoException(R.string.error_bookmark_too_long))
            }
            val updated = bookmark.copy(kind = BookmarkKind.IMAGE, color = null, noteText = noteText)
            return try {
                repository.updateBookmark(updated)
                Result.success(updated)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(JuguitoException(R.string.error_update_bookmark))
            }
        }
        if (bookmark.kind == BookmarkKind.POSITION) {
            return Result.failure(JuguitoException(R.string.error_bookmark_position))
        }
        val color = bookmark.color
            ?: return Result.failure(JuguitoException(R.string.error_bookmark_color))

        val noteText = bookmark.noteText?.trim()?.ifBlank { null }
        if (noteText != null && noteText.length > MAX_BOOKMARK_NOTE) {
            return Result.failure(JuguitoException(R.string.error_bookmark_too_long))
        }

        val kind = if (noteText == null) BookmarkKind.HIGHLIGHT else BookmarkKind.NOTE
        val updated = bookmark.copy(kind = kind, color = color, noteText = noteText)

        return try {
            repository.updateBookmark(updated)
            Result.success(updated)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(JuguitoException(R.string.error_update_bookmark))
        }
    }
}
