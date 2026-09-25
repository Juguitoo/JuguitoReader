package com.juguito.juguitoreader.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import androidx.core.net.toUri
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.data.backup.BackupManifest
import com.juguito.juguitoreader.data.backup.BackupValidation
import com.juguito.juguitoreader.data.backup.relocateInternalPaths
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase
import com.juguito.juguitoreader.data.local.dao.BookDAO
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.repository.BackupRepository
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import com.juguito.juguitoreader.utils.FileUtils.resolveCanonicalFile
import com.juguito.juguitoreader.utils.FileUtils.writeBounded
import com.juguito.juguitoreader.utils.appVersionName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

private const val MAX_ENTRY_BYTES = 512L * 1024 * 1024
private const val MAX_TOTAL_BYTES = 4L * 1024 * 1024 * 1024
private const val MAX_ENTRIES = 8000
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
                    .put("onboardingCompleted", settingsRepository.onboardingCompletedFlow.first())
                    .put("readerGuideCompleted", settingsRepository.readerGuideCompletedFlow.first())
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
        val restoreDir = File(context.cacheDir, "backup-restore/")
        restoreDir.mkdirs()

        try {
            withContext(Dispatchers.IO) {
                val inputStream = context.contentResolver.openInputStream(sourceUri.toUri()) ?: throw JuguitoException(R.string.error_backup_file_not_exists)
                try {
                    ZipInputStream(inputStream).use { zipIn ->
                        var entry = zipIn.nextEntry
                        var entries = 0
                        var totalBytes = 0L
                        while (entry != null) {
                            entries++
                            if (entries > MAX_ENTRIES) throw JuguitoException(R.string.error_import_backup_too_big)
                            if (!entry.isDirectory) {
                                val name = entry.name
                                val allowed = name == "manifest.json" ||
                                    name == "settings.json" ||
                                    name == "database/juguito_db" ||
                                    (name.startsWith("files/") && name != "files/")
                                if (!allowed) {
                                    throw JuguitoException(R.string.error_backup_import_unsupported_format)
                                }
                                val file = resolveCanonicalFile(restoreDir, name)
                                file.parentFile?.mkdirs()
                                writeBounded(zipIn, file, MAX_ENTRY_BYTES)
                                totalBytes += file.length()
                                if (totalBytes > MAX_TOTAL_BYTES) throw JuguitoException(R.string.error_import_backup_too_big)
                            }
                            zipIn.closeEntry()
                            entry = zipIn.nextEntry
                        }
                    }

                    val manifestFile = File(restoreDir, "manifest.json")
                    if (!manifestFile.exists()) throw JuguitoException(R.string.error_backup_import_unsupported_format)
                    val validation = BackupManifest.fromJson(manifestFile.readText()).validate(database.openHelper.writableDatabase.version)
                    when (validation) {
                        is BackupValidation.DbTooNew -> { throw JuguitoException(R.string.error_backup_import_db_too_new) }
                        is BackupValidation.DbTooOld -> { throw JuguitoException(R.string.error_backup_import_db_too_old) }
                        is BackupValidation.UnsupportedFormat -> { throw JuguitoException(R.string.error_backup_import_unsupported_format) }
                        else -> {}
                    }

                    val dbCopy = File(restoreDir, "database/juguito_db")
                    if (!dbCopy.exists()) throw JuguitoException(R.string.error_backup_import_unsupported_format)
                    val sqlite = SQLiteDatabase.openDatabase(
                        dbCopy.absolutePath,
                        null,
                        SQLiteDatabase.OPEN_READWRITE
                    )

                    sqlite.use { db ->
                        db.beginTransaction()
                        try {
                            db.rawQuery("SELECT id, local_file_path, cover_url FROM books", null).use { cursor ->
                                while (cursor.moveToNext()) {
                                    val id = cursor.getInt(0)
                                    val localFilePath = if (cursor.isNull(1)) null else relocateInternalPaths(cursor.getString(1), context.filesDir.absolutePath)
                                    val coverUrl = if (cursor.isNull(2)) null else relocateInternalPaths(cursor.getString(2), context.filesDir.absolutePath)

                                    val values = ContentValues().apply {
                                        if (localFilePath.isNullOrBlank()) putNull("local_file_path") else put("local_file_path", localFilePath)
                                        if (coverUrl.isNullOrBlank()) putNull("cover_url") else put("cover_url", coverUrl)
                                    }

                                    db.update("books", values, "id = ?", arrayOf(id.toString()))
                                }
                            }
                            db.setTransactionSuccessful()
                        } finally {
                            db.endTransaction()
                        }
                    }

                    val liveDb = context.getDatabasePath("juguito_db")
                    database.close()
                    liveDb.delete()
                    File(liveDb.path + "-wal").delete()
                    File(liveDb.path + "-shm").delete()
                    dbCopy.copyTo(liveDb, overwrite = true)
                    File(context.filesDir.path).listFiles { it.name.startsWith("book_") || it.name.startsWith("cover_") }?.forEach { it.delete() }
                    File(restoreDir, "files").listFiles()?.forEach {
                        if (!it.isFile) return@forEach
                        val newFile = File(context.filesDir, it.name)
                        it.copyTo(newFile, overwrite = true)
                    }

                    val settings = File(restoreDir, "settings.json")
                    if (settings.exists()) {
                        val json = JSONObject(settings.readText())
                        settingsRepository.saveTextZoom(json.getInt("textZoom"))
                        settingsRepository.saveReaderTheme(json.getString("readerTheme"))
                        settingsRepository.saveAppTheme(json.getString("appTheme"))
                        settingsRepository.saveReaderBrightness(json.getDouble("readerBrightness").toFloat())
                        settingsRepository.saveLanguage(json.getString("language"))
                        settingsRepository.saveAutoStartReading(json.getBoolean("autoStartReading"))
                        settingsRepository.saveAutoFinishReading(json.getBoolean("autoFinishReading"))
                        settingsRepository.savePromptStatusChange(json.getBoolean("promptStatusChange"))
                        if (json.has("lastSeenChangelogVersion") && !json.getString("lastSeenChangelogVersion").isNullOrBlank()) {
                            settingsRepository.saveLastSeenChangelogVersion(json.getString("lastSeenChangelogVersion"))
                        }
                        if (json.has("onboardingCompleted")) {
                            settingsRepository.saveOnboardingCompleted(json.getBoolean("onboardingCompleted"))
                        }
                        if (json.has("readerGuideCompleted")) {
                            settingsRepository.saveReaderGuideCompleted(json.getBoolean("readerGuideCompleted"))
                        }
                    }

                } catch (_: SecurityException) {
                    throw JuguitoException(R.string.error_backup_import_unsupported_format)
                } catch (_: JSONException) {
                    throw JuguitoException(R.string.error_backup_import_unsupported_format)
                } catch (_: SQLiteException) {
                    throw JuguitoException(R.string.something_went_wrong)
                }
            }
        } finally {
            restoreDir.deleteRecursively()
        }
    }
}