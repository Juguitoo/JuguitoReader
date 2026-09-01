package com.juguito.juguitoreader.domain.usecase.book

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.DeletedBookSnapshot
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.domain.repository.FolderRepository
import com.juguito.juguitoreader.domain.repository.GenreRepository
import javax.inject.Inject

class RestoreDeletedBookUseCase @Inject constructor(
    private val bookRepository: BookRepository,
    private val folderRepository: FolderRepository,
    private val genreRepository: GenreRepository
) {
    suspend operator fun invoke(snapshot: DeletedBookSnapshot): Result<Unit> {
        return try {
            val folderIds = resolveFolderIds(snapshot.book.folders, folderRepository)
            val genreIds = resolveGenreIds(snapshot.book.genres, genreRepository)

            bookRepository.restoreDeletedBook(snapshot, folderIds, genreIds)
            Result.success(Unit)
        } catch (_: Exception) {
            Result.failure(JuguitoException(R.string.error_restore_book))
        }
    }
}