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
            val folderIds = book.folders.map { folder ->
                val existingFolder = folderRepository.getFolderByName(folder.name)
                existingFolder?.id ?: folderRepository.insertFolder(folder).toInt()
            }

            val genreIds = book.genres.map { genre ->
                val existingGenre = genreRepository.getGenreByName(genre.name)
                existingGenre?.id ?: genreRepository.saveGenre(genre).toInt()
            }

            val bookId = bookRepository.saveBook(book).toInt()

            bookRepository.addCrossReferences(bookId, folderIds, genreIds)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(JuguitoException(R.string.error_save_book))
        }
    }
}
