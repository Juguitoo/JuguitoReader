package com.juguito.juguitoreader.data.local

import androidx.room.TypeConverter
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.enums.BookmarkColor
import com.juguito.juguitoreader.domain.enums.BookmarkKind

class RoomConverters {

    @TypeConverter
    fun fromBookStatus(value: BookStatus): String {
        return value.name
    }

    @TypeConverter
    fun toBookStatus(value: String): BookStatus {
        return enumValueOf<BookStatus>(value)
    }

    @TypeConverter
    fun fromBookmarkKind(value: BookmarkKind?): String? {
        return value?.name
    }

    @TypeConverter
    fun toBookmarkKind(value: String?): BookmarkKind? {
        return value?.let{
            enumValueOf<BookmarkKind>(it)
        }
    }

    @TypeConverter
    fun fromBookmarkColor(value: BookmarkColor?): String? {
        return value?.name
    }

    @TypeConverter
    fun toBookmarkColor(value: String?): BookmarkColor? {
        return value?.let {
            enumValueOf<BookmarkColor>(it)
        }
    }
}