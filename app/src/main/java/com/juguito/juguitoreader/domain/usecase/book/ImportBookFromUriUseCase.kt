package com.juguito.juguitoreader.domain.usecase.book

import android.content.Context
import android.net.Uri
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
            Result.failure(e)
        }
    }
}
