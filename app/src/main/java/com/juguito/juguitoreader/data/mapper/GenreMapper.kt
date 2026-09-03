package com.juguito.juguitoreader.data.mapper

import com.juguito.juguitoreader.data.local.entity.GenreEntity
import com.juguito.juguitoreader.data.local.entity.GenreWithCountEntity
import com.juguito.juguitoreader.domain.model.Genre

fun GenreEntity.toDomain(): Genre {
    return Genre(
        id = id,
        name = name,
        createdAt = createdAt
    )
}

fun Genre.toEntity(): GenreEntity {
    return GenreEntity(
        id = id,
        name = name,
        createdAt = createdAt
    )
}

fun GenreWithCountEntity.toDomain(): Genre {
    return Genre(
        id = genre.id,
        name = genre.name,
        bookCount = bookCount,
        createdAt = genre.createdAt
    )
}
