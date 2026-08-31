package com.juguito.juguitoreader.domain.usecase.book

import android.content.Context
import android.net.Uri
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.utils.FileUtils.deleteFileFromInternalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImportBookFromUriUseCase @Inject constructor(
    private val getBookFromEpubUseCase: GetBookFromEpubUseCase,
    private val addBookUseCase: AddBookUseCase
) {
    suspend operator fun invoke(context: Context, uri: Uri): Result<Unit> {
        var book: Book? = null
        return try {
            book = getBookFromEpubUseCase(context, uri, persistFiles = true)
            val result = addBookUseCase(book)
            if (result.isFailure) {
                rollbackImportedFiles(context, book)
            }
            result
        } catch (_: Exception) {
            book?.let { rollbackImportedFiles(context, it) }
            Result.failure(JuguitoException(R.string.error_import_book))
        }
    }

    private suspend fun rollbackImportedFiles(context: Context, book: Book) {
        withContext(Dispatchers.IO) {
            deleteFileFromInternalStorage(context, book.localFilePath)
            deleteFileFromInternalStorage(context, book.coverUrl)
        }
    }
}
