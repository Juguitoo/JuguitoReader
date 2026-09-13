package com.juguito.juguitoreader.data.repository

import android.content.Context
import androidx.core.net.toUri
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.data.backup.BackupManifest
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase
import com.juguito.juguitoreader.data.local.dao.BookDAO
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.repository.BackupRepository
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import com.juguito.juguitoreader.utils.appVersionName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookDAO: BookDAO,
    private val settingsRepository: SettingsRepository,
    private val database: JuguitoReaderDatabase
) : BackupRepository {
    override suspend fun exportTo(destinationUri: String) {
        withContext(Dispatchers.IO) {
            database.openHelper.writableDatabase
                .query("PRAGMA wal_checkpoint(FULL)")
                .use { it.moveToFirst() }

            val dbFile = context.getDatabasePath("juguito_db")
            if (!dbFile.exists()) throw JuguitoException(R.string.error_backup_export)

            val cacheFile = File(context.cacheDir, "backup-export/juguito_db")
            cacheFile.parentFile?.mkdirs()
            dbFile.copyTo(cacheFile, overwrite = true)

            try {
                val books = bookDAO.getAllBooks().first()
                val epubFiles = books.mapNotNull {
                    val path = it.book.localFilePath ?: return@mapNotNull null
                    if (!File(path).exists() || !path.startsWith(context.filesDir.absolutePath)) return@mapNotNull null
                    File(path)
                }

                val coverFiles = books.mapNotNull {
                    val path = it.book.coverUrl ?: return@mapNotNull null
                    if (!File(path).exists() || !path.startsWith(context.filesDir.absolutePath)) return@mapNotNull null
                    File(path)
                }

                val files = (epubFiles + coverFiles).distinct()

                val settingsJson = JSONObject()
                    .put("textZoom", settingsRepository.textZoomFlow.first())
                    .put("readerTheme", settingsRepository.readerThemeFlow.first())
                    .put("appTheme", settingsRepository.appThemeFlow.first())
                    .put("readerBrightness", settingsRepository.readerBrightnessFlow.first())
                    .put("language", settingsRepository.languageFlow.first())
                    .put("autoStartReading", settingsRepository.autoStartReadingFlow.first())
                    .put("autoFinishReading", settingsRepository.autoFinishReadingFlow.first())
                    .put("promptStatusChange", settingsRepository.promptStatusChangeFlow.first())
                    .put("lastSeenChangelogVersion", settingsRepository.lastSeenChangelogVersionFlow.first() ?: JSONObject.NULL)
                    .toString()

                val backupManifest = BackupManifest(
                    formatVersion = 1,
                    appVersionName = context.appVersionName(),
                    dbVersion = database.openHelper.writableDatabase.version,
                    createdAtEpochMs = System.currentTimeMillis(),
                    bookFileCount = epubFiles.count(),
                    coverFileCount = coverFiles.count()
                ).toJson()

                val outputStream = context.contentResolver.openOutputStream(destinationUri.toUri()) ?: throw JuguitoException(R.string.error_backup_export)
                ZipOutputStream(outputStream).use { zipOut ->
                    zipOut.putNextEntry(ZipEntry("manifest.json"))
                    zipOut.write(backupManifest.toByteArray(Charsets.UTF_8))
                    zipOut.closeEntry()

                    zipOut.putNextEntry(ZipEntry("settings.json"))
                    zipOut.write(settingsJson.toByteArray(Charsets.UTF_8))
                    zipOut.closeEntry()

                    FileInputStream(cacheFile).use { inputStream ->
                        zipOut.putNextEntry(ZipEntry("database/juguito_db"))
                        inputStream.copyTo(zipOut)
                        zipOut.closeEntry()
                    }

                    files.forEach { file ->
                        FileInputStream(file).use { inputStream ->
                            zipOut.putNextEntry(ZipEntry("files/${file.name}"))
                            inputStream.copyTo(zipOut)
                            zipOut.closeEntry()
                        }
                    }
                }
            } finally {
                cacheFile.parentFile?.deleteRecursively()
            }
        }
    }

    override suspend fun importFrom(sourceUri: String) {
        TODO("Not yet implemented")
    }
}