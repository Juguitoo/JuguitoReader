package com.juguito.juguitoreader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.juguito.juguitoreader.domain.model.SyncStatus

@Entity(
    tableName = "folders",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String? = null,
    @ColumnInfo(name = "color_hex") val colorHex: String,
    @ColumnInfo(name = "sync_status") val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
