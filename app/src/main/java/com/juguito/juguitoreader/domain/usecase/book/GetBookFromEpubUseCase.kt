package com.juguito.juguitoreader.domain.usecase.book

import android.content.Context
import android.net.Uri
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.utils.EpubParser
import com.juguito.juguitoreader.utils.FileUtils
import javax.inject.Inject

class GetBookFromEpubUseCase @Inject constructor() {
    operator fun invoke(context: Context, uri: Uri): Book {
        val metadata = EpubParser.extractMetadata(context, uri)
        val internalPath = FileUtils.saveBookToInternalStorage(context, uri)
        
        return Book(
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
