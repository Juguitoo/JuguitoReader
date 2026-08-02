package com.juguito.juguitoreader.domain.usecase.book

import com.juguito.juguitoreader.domain.repository.BookRepository
import javax.inject.Inject

class DeleteBookUseCase @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> {
        return try {
            repository.deleteBook(bookId)
            Result.success(Unit)
        } catch (e: Exception){
            Result.failure(Exception("Error al borrar el libro: ${e.localizedMessage}"))
        }
    }
}