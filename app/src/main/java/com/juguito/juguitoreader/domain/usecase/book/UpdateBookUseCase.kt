package com.juguito.juguitoreader.domain.usecase.book

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.domain.repository.FolderRepository
import com.juguito.juguitoreader.domain.repository.GenreRepository
import javax.inject.Inject

class UpdateBookUseCase @Inject constructor(
    private val bookRepository: BookRepository,
    private val folderRepository: FolderRepository,
    private val genreRepository: GenreRepository
) {
    suspend operator fun invoke(book: Book) : Result<Unit>{
        if (book.title.isBlank()) {
            return Result.failure(JuguitoException(R.string.error_title_empty))
        } else if (book.author.isBlank()) {
            return Result.failure(JuguitoException(R.string.error_author_empty))
        }

        return try {
            val persistedBook = bookRepository.getBookById(book.id) ?: throw Exception()

            val folderIds = resolveFolderIds(book.folders, folderRepository)
            val genreIds = resolveGenreIds(book.genres, genreRepository)
            if (persistedBook.localFilePath == book.localFilePath) {
                bookRepository.updateBookWithCrossRefs(book, folderIds, genreIds)
            } else {
                bookRepository.updateBookWithNewEpub(book, folderIds, genreIds)
            }
            Result.success(Unit)
        } catch (_: Exception) {
            Result.failure(JuguitoException(R.string.error_save_book))
        }
    }
}
