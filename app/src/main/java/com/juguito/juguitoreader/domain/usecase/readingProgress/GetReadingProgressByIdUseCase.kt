package com.juguito.juguitoreader.domain.usecase.readingProgress

import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.repository.ReadingProgressRepository
import javax.inject.Inject

class GetReadingProgressByIdUseCase @Inject constructor(
    private val repository: ReadingProgressRepository
) {
    suspend operator fun invoke(bookId: Int): ReadingProgress? {
        return repository.getReadingProgressById(bookId)
    }
}