package com.juguito.juguitoreader.data.backup

import org.json.JSONObject

sealed interface BackupValidation {
    data object OK: BackupValidation
    data object UnsupportedFormat: BackupValidation
    data object DbTooNew: BackupValidation
    data object DbTooOld: BackupValidation
}

data class BackupManifest(
    val formatVersion: Int,
    val appVersionName: String,
    val dbVersion: Int,
    val createdAtEpochMs: Long,
    val bookFileCount: Int,
    val coverFileCount: Int
) {
    internal val FORMAT_VERSION = 1
    internal val MIN_DB_VERSION = 6

    fun toJson(): String {
        return JSONObject()
            .put("formatVersion", formatVersion)
            .put("appVersionName", appVersionName)
            .put("dbVersion", dbVersion)
            .put("createdAtEpochMs", createdAtEpochMs)
            .put("bookFileCount", bookFileCount)
            .put("coverFileCount", coverFileCount)
            .toString()
    }

    companion object {
        fun fromJson(input: String): BackupManifest {
            val parsed = JSONObject(input)

            val formatVersion = parsed.getInt("formatVersion")
            val appVersionName = parsed.getString("appVersionName")
            val dbVersion = parsed.getInt("dbVersion")
            val createdAtEpochMs = parsed.getLong("createdAtEpochMs")
            val bookFileCount = parsed.getInt("bookFileCount")
            val coverFileCount = parsed.getInt("coverFileCount")

            return BackupManifest(
                formatVersion,
                appVersionName,
                dbVersion,
                createdAtEpochMs,
                bookFileCount,
                coverFileCount
            )
        }
    }

    fun validate(currentDbVersion: Int): BackupValidation {
        if (formatVersion != FORMAT_VERSION) return BackupValidation.UnsupportedFormat
        if (dbVersion < MIN_DB_VERSION) return BackupValidation.DbTooOld
        if (dbVersion > currentDbVersion) return BackupValidation.DbTooNew
        return BackupValidation.OK
    }
}