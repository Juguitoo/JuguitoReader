package com.juguito.juguitoreader.data.mapper

import com.juguito.juguitoreader.data.local.entity.GenreEntity
import com.juguito.juguitoreader.domain.model.Genre

fun GenreEntity.toDomain(): Genre {
    return Genre(
        id = id,
        name = name,
        syncStatus = syncStatus,
        createdAt = createdAt
    )
}

fun Genre.toEntity(): GenreEntity {
    return GenreEntity(
        id = id,
        name = name,
        syncStatus = syncStatus,
        createdAt = createdAt
    )
}