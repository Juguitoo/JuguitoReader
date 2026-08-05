package com.juguito.juguitoreader.domain.usecase.book

import android.content.Context
import android.net.Uri
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.utils.EpubParser
import javax.inject.Inject

class GetBookFromEpubUseCase @Inject constructor() {
    operator fun invoke(context: Context, uri: Uri): Book {
        val metadata = EpubParser.extractMetadata(context, uri)
        return Book(
            title = metadata.title ?: "",
            author = metadata.author ?: "",
            publisher = metadata.publisher,
            isPhysical = false,
            coverUrl = metadata.coverUrl,
            localFilePath = uri.toString(),
            genres = metadata.genres.map { Genre(name = it) }
        )
    }
}
