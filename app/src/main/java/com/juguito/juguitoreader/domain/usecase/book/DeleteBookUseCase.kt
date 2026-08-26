package com.juguito.juguitoreader.domain.usecase.book

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.repository.BookRepository
import javax.inject.Inject

class DeleteBookUseCase @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> {
        return try {
            repository.getBookById(bookId) ?: return Result.failure(JuguitoException(R.string.error_delete_book_not_found))

            repository.deleteBook(bookId)
            
            Result.success(Unit)
        } catch (e: Exception){
            e.printStackTrace()
            Result.failure(JuguitoException(R.string.error_delete_book))
        }
    }
}
