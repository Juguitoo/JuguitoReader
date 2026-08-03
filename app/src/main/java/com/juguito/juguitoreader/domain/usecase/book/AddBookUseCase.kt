package com.juguito.juguitoreader.domain.usecase.book

import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.domain.repository.FolderRepository
import com.juguito.juguitoreader.domain.repository.GenreRepository
import javax.inject.Inject

class AddBookUseCase @Inject constructor(
    private val bookRepository: BookRepository,
    private val folderRepository: FolderRepository,
    private val genreRepository: GenreRepository
) {
    suspend operator fun invoke(book: Book): Result<Unit> {
        if (book.title.isBlank()) {
            return Result.failure(Exception("El título del libro no puede estar vacío."))
        } else if (book.author.isBlank()) {
            return Result.failure(Exception("El autor del libro no puede estar vacío."))
        }

        return try {
            val folderIds = book.folders.map { folder ->
                val existingFolder = folderRepository.getFolderByName(folder.name)
                existingFolder?.id ?: folderRepository.saveFolder(folder).toInt()
            }

            val genreIds = book.genres.map { genre ->
                val existingGenre = genreRepository.getGenreByName(genre.name)
                existingGenre?.id ?: genreRepository.saveGenre(genre).toInt()
            }

            val bookId = bookRepository.saveBook(book).toInt()

            bookRepository.addCrossReferences(bookId, folderIds, genreIds)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al guardar el libro: ${e.localizedMessage}"))
        }
    }
}