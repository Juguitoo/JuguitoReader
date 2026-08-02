package com.juguito.juguitoreader.domain.usecase.book

import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.repository.BookRepository
import javax.inject.Inject

class AddBookUseCase @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(book: Book): Result<Unit> {
        if (book.title.isBlank()){
            return Result.failure(Exception("El título del libro no puede estar vacío."))
        } else if (book.author.isBlank()){
            return Result.failure(Exception("El autor del libro no puede estar vacío."))
        }

        return try {
            repository.saveBook(book)
            Result.success(Unit)
        } catch (e: Exception){
            Result.failure(Exception("Error al guardar el libro: ${e.localizedMessage}"))
        }
    }
}