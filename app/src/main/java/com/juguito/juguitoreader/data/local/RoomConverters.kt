package com.juguito.juguitoreader.data.local

import androidx.room.TypeConverter
import com.juguito.juguitoreader.domain.enums.BookStatus

class RoomConverters {

    @TypeConverter
    fun fromBookStatus(value: BookStatus): String {
        return value.name
    }

    @TypeConverter
    fun toBookStatus(value: String): BookStatus {
        return enumValueOf<BookStatus>(value)
    }
}