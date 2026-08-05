package com.juguito.juguitoreader.data.mapper

import com.juguito.juguitoreader.data.local.entity.BookEntity
import com.juguito.juguitoreader.data.local.entity.BookWithDetails
import com.juguito.juguitoreader.domain.model.Book

fun BookWithDetails.toDomain(): Book {
    return Book(
        id = book.id,
        title = book.title,
        author = book.author,
        publisher = book.publisher,
        isPhysical = book.isPhysical,
        status = book.status,
        rating = book.rating,
        comment = book.comment,
        startDate = book.startDate,
        endDate = book.endDate,
        coverUrl = book.coverUrl,
        localFilePath = book.localFilePath,
        syncStatus = book.syncStatus,
        createdAt = book.createdAt,
        folders = folders.map{ it.toDomain() },
        genres = genres.map{ it.toDomain() }
    )
}

fun Book.toEntity(): BookEntity {
    return BookEntity(
        id = id,
        title = title,
        author = author,
        publisher = publisher,
        isPhysical = isPhysical,
        status = status,
        rating = rating,
        comment = comment,
        startDate = startDate,
        endDate = endDate,
        coverUrl = coverUrl,
        localFilePath = localFilePath,
        syncStatus = syncStatus,
        createdAt = createdAt
    )
}