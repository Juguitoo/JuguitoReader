package com.juguito.juguitoreader.domain.usecase.book

import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.repository.BookRepository
import javax.inject.Inject

class GetBookByIdUseCase @Inject constructor(
    private val repository: BookRepository
) {

    suspend operator fun invoke(bookId: Int): Book? {
        return repository.getBookById(bookId)
    }
}