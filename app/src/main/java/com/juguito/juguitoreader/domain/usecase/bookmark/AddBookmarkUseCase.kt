package com.juguito.juguitoreader.domain.usecase.bookmark

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookmarkKind
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Bookmark
import com.juguito.juguitoreader.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddBookmarkUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    suspend operator fun invoke(draft: NewBookmark): Result<Int> {
        val quote = draft.quote.trim()
        val chapterHref = draft.chapterHref.trim()
        val imageSrc = when (draft) {
            is NewBookmark.Position -> draft.imageSrc?.trim()?.ifBlank { null }
            is NewBookmark.Image -> draft.imageSrc.trim().ifBlank { null }
            else -> null
        }
        val noteText = (draft as? NewBookmark.Note)?.noteText?.trim()

        if (draft is NewBookmark.Image && imageSrc == null) {
            return Result.failure(JuguitoException(R.string.error_bookmark_empty_text))
        }
        if (draft !is NewBookmark.Position && draft !is NewBookmark.Image && quote.isBlank()) {
            return Result.failure(JuguitoException(R.string.error_bookmark_empty_text))
        }
        if (draft is NewBookmark.Position &&
            quote.isBlank() &&
            imageSrc == null &&
            draft.prefix.isBlank() &&
            draft.suffix.isBlank()
        ) {
            return Result.failure(JuguitoException(R.string.error_bookmark_empty_text))
        }
        if (quote.length > MAX_BOOKMARK_QUOTE ||
            draft.prefix.length > MAX_BOOKMARK_CONTEXT ||
            draft.suffix.length > MAX_BOOKMARK_CONTEXT ||
            (noteText != null && noteText.length > MAX_BOOKMARK_NOTE)
        ) {
            return Result.failure(JuguitoException(R.string.error_bookmark_too_long))
        }

        val kind = when (draft) {
            is NewBookmark.Highlight -> BookmarkKind.HIGHLIGHT
            is NewBookmark.Note -> BookmarkKind.NOTE
            is NewBookmark.Position -> BookmarkKind.POSITION
            is NewBookmark.Image -> BookmarkKind.IMAGE
        }
        val color = when (draft) {
            is NewBookmark.Highlight -> draft.color
            is NewBookmark.Note -> draft.color
            is NewBookmark.Position -> null
            is NewBookmark.Image -> null
        }

        val existingMarks = repository.getBookBookmarks(draft.bookId).first()
        val duplicate = if (draft is NewBookmark.Image) {
            existingMarks.any { existing ->
                existing.kind == BookmarkKind.IMAGE &&
                    existing.chapterHref == chapterHref &&
                    existing.imageSrc == imageSrc
            }
        } else {
            existingMarks.any { existing ->
                existing.kind == kind &&
                    existing.chapterHref == chapterHref &&
                    existing.quote == quote &&
                    existing.prefix == draft.prefix &&
                    existing.imageSrc == imageSrc
            }
        }
        if (duplicate) {
            return Result.failure(JuguitoException(R.string.error_bookmark_duplicate))
        }

        val bookmark = Bookmark(
            bookId = draft.bookId,
            kind = kind,
            chapterHref = chapterHref,
            quote = quote,
            prefix = draft.prefix,
            suffix = draft.suffix,
            imageSrc = imageSrc,
            color = color,
            noteText = noteText,
            scrollPosition = draft.scrollPosition,
            createdAt = System.currentTimeMillis()
        )

        return try {
            val id = repository.insertBookmark(bookmark).toInt()
            Result.success(id)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(JuguitoException(R.string.error_add_bookmark))
        }
    }
}
