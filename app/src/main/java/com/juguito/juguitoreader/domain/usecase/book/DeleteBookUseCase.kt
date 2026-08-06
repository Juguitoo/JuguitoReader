package com.juguito.juguitoreader.domain.usecase.book

import android.content.Context
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.utils.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeleteBookUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: BookRepository
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> {
        return try {
            val book = repository.getBookById(bookId)

            book?.coverUrl?.let { path ->
                FileUtils.deleteFileFromInternalStorage(context, path)
            }

            book?.localFilePath?.let { path ->
                FileUtils.deleteFileFromInternalStorage(context, path)
            }

            repository.deleteBook(bookId)
            
            Result.success(Unit)
        } catch (e: Exception){
            Result.failure(Exception("Error al borrar el libro: ${e.localizedMessage}"))
        }
    }
}
