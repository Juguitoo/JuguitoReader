package com.juguito.juguitoreader.domain.usecase.book

import android.content.Context
import android.net.Uri
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.utils.EpubParser
import com.juguito.juguitoreader.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetBookFromEpubUseCase @Inject constructor() {
    suspend operator fun invoke(context: Context, uri: Uri): Book {
        return withContext(Dispatchers.IO) {
            val metadata = EpubParser.extractMetadata(context, uri)
            val internalPath = FileUtils.saveBookToInternalStorage(context, uri)

            return@withContext Book(
                title = metadata.title ?: "",
                author = metadata.author ?: "",
                publisher = metadata.publisher,
                series = metadata.series,
                seriesOrder = metadata.seriesOrder,
                isPhysical = false,
                coverUrl = metadata.coverUrl,
                localFilePath = internalPath ?: throw JuguitoException(R.string.error_copy_epub),
                genres = metadata.genres.map { Genre(name = it) }
            )
        }
    }
}
