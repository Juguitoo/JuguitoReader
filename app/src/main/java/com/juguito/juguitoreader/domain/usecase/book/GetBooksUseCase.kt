package com.juguito.juguitoreader.domain.usecase.book

import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBooksUseCase @Inject constructor(
    private val repository: BookRepository
) {
    operator fun invoke(): Flow<List<Book>> {
        return repository.getAllBooks()
    }
}