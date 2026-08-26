package com.juguito.juguitoreader.domain.usecase.book

import android.content.Context
import android.net.Uri
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import javax.inject.Inject

class ImportBookFromUriUseCase @Inject constructor(
    private val getBookFromEpubUseCase: GetBookFromEpubUseCase,
    private val addBookUseCase: AddBookUseCase
) {
    suspend operator fun invoke(context: Context, uri: Uri): Result<Unit> {
        return try {
            val book = getBookFromEpubUseCase(context, uri)
            addBookUseCase(book)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(JuguitoException(R.string.error_import_book))
        }
    }
}
