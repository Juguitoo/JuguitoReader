package com.juguito.juguitoreader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.juguito.juguitoreader.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDAO {

    @Insert
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Insert
    suspend fun insertBookmarks(bookmarks: List<BookmarkEntity>)

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmark WHERE book_id = :bookId")
    fun getBookBookmarks(bookId: Int): Flow<List<BookmarkEntity>>

    @Query("DELETE FROM bookmark WHERE id = :id")
    suspend fun deleteBookmark(id: Int)

    @Query("DELETE FROM bookmark WHERE book_id = :bookId")
    suspend fun deleteBookBookmarks(bookId: Int)
}