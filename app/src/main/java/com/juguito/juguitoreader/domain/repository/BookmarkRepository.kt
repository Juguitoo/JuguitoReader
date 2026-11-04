package com.juguito.juguitoreader.domain.repository

import com.juguito.juguitoreader.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {

    fun getBookBookmarks(bookId: Int): Flow<List<Bookmark>>

    suspend fun insertBookmark(bookmark: Bookmark): Long
    
    suspend fun insertBookmarks(bookmarks: List<Bookmark>)

    suspend fun updateBookmark(bookmark: Bookmark)

    suspend fun deleteBookmark(id: Int)

    suspend fun deleteBookBookmarks(bookId: Int)
}