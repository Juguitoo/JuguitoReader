package com.juguito.juguitoreader.data.mapper

import com.juguito.juguitoreader.data.local.entity.FolderEntity
import com.juguito.juguitoreader.domain.model.Folder

fun FolderEntity.toDomain(): Folder {
    return Folder(
        id = id,
        name = name,
        colorHex = colorHex,
        description = description,
        syncStatus = syncStatus
    )
}

fun Folder.toEntity(): FolderEntity{
    return FolderEntity(
        id = id,
        name = name,
        colorHex = colorHex,
        description = description,
        syncStatus = syncStatus
    )
}