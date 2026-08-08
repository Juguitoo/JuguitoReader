package com.juguito.juguitoreader.domain.usecase.reader

import android.content.Context
import com.juguito.juguitoreader.domain.model.EpubContent
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.utils.EpubParser
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject

class ParseEpubUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(bookId: Int, localFilePath: String): Result<EpubContent> {
        bookRepository.getBookById(bookId) ?: return Result.failure(Exception("El libro del fichero epub no existe."))

        return try {
            Result.success(EpubParser.extractFullContent(context, bookId, localFilePath))
        } catch (ioException: IOException) {
            return Result.failure(Exception(ioException.localizedMessage))
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(Exception("Ha ocurrido un error al extraer los datos del fichero epub. Intentalo de nuevo reemplazando el fichero epub por otro distinto."))
        }
    }
}