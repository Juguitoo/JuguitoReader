package com.juguito.juguitoreader.domain.usecase.book

import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.FolderRepository
import com.juguito.juguitoreader.domain.repository.GenreRepository

internal suspend fun resolveFolderIds(folders: List<Folder>, folderRepository: FolderRepository) : List<Int> {
    return folders.map { folder ->
        if (folder.id != 0) {
            val existing = folderRepository.getFolderById(folder.id)
            if (existing != null) return@map existing.id
        }
        val byName = folderRepository.getFolderByName(folder.name)
        return@map byName?.id ?: folderRepository.insertFolder(folder).toInt()
    }
}

internal suspend fun resolveGenreIds(genres: List<Genre>, genreRepository: GenreRepository) : List<Int> {
    return genres.map { genre ->
        if (genre.id != 0) {
            val existing = genreRepository.getGenreById(genre.id)
            if (existing != null) return@map existing.id
        }
        val byName = genreRepository.getGenreByName(genre.name)
        return@map byName?.id ?: genreRepository.insertGenre(genre).toInt()
    }
}