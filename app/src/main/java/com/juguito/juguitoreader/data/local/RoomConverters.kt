package com.juguito.juguitoreader.data.local

import androidx.room.TypeConverter
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.enums.SyncStatus

class RoomConverters {

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String {
        return value.name
    }

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return enumValueOf<SyncStatus>(value)
    }

    @TypeConverter
    fun fromBookStatus(value: BookStatus): String {
        return value.name
    }

    @TypeConverter
    fun toBookStatus(value: String): BookStatus {
        return enumValueOf<BookStatus>(value)
    }
}