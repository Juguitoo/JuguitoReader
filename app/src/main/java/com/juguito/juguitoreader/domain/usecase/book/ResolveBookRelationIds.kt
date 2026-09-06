package com.juguito.juguitoreader.domain.usecase.book

import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.FolderRepository
import com.juguito.juguitoreader.domain.repository.GenreRepository

internal suspend fun resolveFolderIds(folders: List<Folder>, folderRepository: FolderRepository): List<Int> {
    return folders.mapNotNull { folder ->
        if (folder.id != 0) {
            folderRepository.getFolderById(folder.id)?.id
        } else {
            folderRepository.getFolderByName(folder.name)?.id
                ?: folderRepository.insertFolder(folder).toInt()
        }
    }
}

internal suspend fun resolveGenreIds(genres: List<Genre>, genreRepository: GenreRepository): List<Int> {
    return genres.mapNotNull { genre ->
        if (genre.id != 0) {
            genreRepository.getGenreById(genre.id)?.id
        } else {
            genreRepository.getGenreByName(genre.name)?.id
                ?: genreRepository.insertGenre(genre).toInt()
        }
    }
}
