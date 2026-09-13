package com.juguito.juguitoreader.domain.repository

interface BackupRepository {
    suspend fun exportTo(destinationUri: String)
    suspend fun importFrom(sourceUri: String)
}