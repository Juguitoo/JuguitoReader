package com.juguito.juguitoreader.domain.usecase.reader

import android.content.Context
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
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
        bookRepository.getBookById(bookId) ?: return Result.failure(JuguitoException(R.string.error_epub_not_found))

        return try {
            Result.success(EpubParser.extractFullContent(context, bookId, localFilePath))
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return Result.failure(JuguitoException(R.string.something_went_wrong))
        } catch (securityException: SecurityException) {
            securityException.printStackTrace()
            return Result.failure(JuguitoException(R.string.something_went_wrong))
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(JuguitoException(R.string.error_epub_extraction))
        }
    }
}
