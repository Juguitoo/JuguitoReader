package com.juguito.juguitoreader.domain.usecase.book

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.withoutEpubIfPhysical
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
            val toSave = book.withoutEpubIfPhysical()

            val folderIds = resolveFolderIds(toSave.folders, folderRepository)
            val genreIds = resolveGenreIds(toSave.genres, genreRepository)
            val replacingEpub = persistedBook.localFilePath != toSave.localFilePath &&
                toSave.localFilePath != null
            if (replacingEpub) {
                bookRepository.updateBookWithNewEpub(toSave, folderIds, genreIds)
            } else {
                bookRepository.updateBookWithCrossRefs(toSave, folderIds, genreIds)
            }
            Result.success(Unit)
        } catch (_: Exception) {
            Result.failure(JuguitoException(R.string.error_save_book))
        }
    }
}
