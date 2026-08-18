package com.juguito.juguitoreader.data.mapper

import com.juguito.juguitoreader.data.local.entity.FolderEntity
import com.juguito.juguitoreader.data.local.entity.FolderWithCountEntity
import com.juguito.juguitoreader.domain.model.Folder

fun FolderEntity.toDomain(): Folder {
    return Folder(
        id = id,
        name = name,
        colorHex = colorHex,
        description = description,
        syncStatus = syncStatus,
        createdAt = createdAt
    )
}

fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        description = description,
        colorHex = colorHex,
        syncStatus = syncStatus,
        createdAt = createdAt
    )
}

fun FolderWithCountEntity.toDomain(): Folder {
    return Folder(
        id = folder.id,
        name = folder.name,
        colorHex = folder.colorHex,
        description = folder.description,
        bookCount = bookCount,
        syncStatus = folder.syncStatus,
        createdAt = folder.createdAt
    )
}