package com.juguito.juguitoreader.data.repository

import com.juguito.juguitoreader.data.local.dao.BookmarkDAO
import com.juguito.juguitoreader.data.mapper.toDomain
import com.juguito.juguitoreader.data.mapper.toEntity
import com.juguito.juguitoreader.domain.model.Bookmark
import com.juguito.juguitoreader.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDAO: BookmarkDAO
) : BookmarkRepository {
    override fun getBookBookmarks(bookId: Int): Flow<List<Bookmark>> {
        return bookmarkDAO.getBookBookmarks(bookId).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun insertBookmark(bookmark: Bookmark): Long {
        return bookmarkDAO.insertBookmark(bookmark.toEntity())
    }

    override suspend fun insertBookmarks(bookmarks: List<Bookmark>) {
        bookmarkDAO.insertBookmarks(bookmarks.map { it.toEntity() })
    }

    override suspend fun updateBookmark(bookmark: Bookmark) {
        bookmarkDAO.updateBookmark(bookmark.toEntity())
    }

    override suspend fun deleteBookmark(id: Int) {
        bookmarkDAO.deleteBookmark(id)
    }

    override suspend fun deleteBookBookmarks(bookId: Int) {
        bookmarkDAO.deleteBookBookmarks(bookId)
    }

}