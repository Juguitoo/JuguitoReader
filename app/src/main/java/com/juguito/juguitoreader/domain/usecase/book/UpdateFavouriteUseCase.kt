package com.juguito.juguitoreader.domain.usecase.book

import com.juguito.juguitoreader.domain.repository.BookRepository
import javax.inject.Inject

class UpdateFavouriteUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(bookId: Int, newValue: Boolean) {
        bookRepository.updateFavourite(bookId, newValue)
    }
}