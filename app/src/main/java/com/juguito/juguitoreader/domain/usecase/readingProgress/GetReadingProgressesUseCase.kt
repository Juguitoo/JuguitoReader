package com.juguito.juguitoreader.domain.usecase.readingProgress

import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.repository.ReadingProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReadingProgressesUseCase @Inject constructor(
    private val repository: ReadingProgressRepository
) {
     operator fun invoke(): Flow<List<ReadingProgress>> {
        return repository.getAllReadingProgress()

    }
}