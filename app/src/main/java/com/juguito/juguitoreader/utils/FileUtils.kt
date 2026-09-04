package com.juguito.juguitoreader.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class PromotedBookFiles(val epubPath: String?, val coverPath: String?)

object FileUtils {
    fun getTempImageUri(context: Context): Uri {
        val directory = File(context.cacheDir, "images")
        directory.mkdirs()
        val file = File(directory, "temp_image_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "com.juguito.juguitoreader.fileprovider",
            file
        )
    }

    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        if (uri.path?.startsWith(context.filesDir.absolutePath) == true) {
            return uri.path
        }

        return try {
            val fileName = "cover_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val stream = context.contentResolver.openInputStream(uri) ?: throw IOException("Error al abrir el stream")
            stream.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            throw JuguitoException(R.string.error_copy_cover)
        }
    }

    fun saveEpubBookToInternalStorage(context: Context, uri: Uri, extension: String = "epub"): String? {
        if (uri.path?.startsWith(context.filesDir.absolutePath) == true) {
            return uri.path
        }

        return try {
            val fileName = "book_${System.currentTimeMillis()}.$extension"
            val file = File(context.filesDir, fileName)

            val stream = context.contentResolver.openInputStream(uri) ?: throw IOException("Error al abrir el stream")
            stream.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            throw JuguitoException(R.string.error_copy_epub)
        }
    }

    fun deleteFileFromInternalStorage(context: Context, path: String?) {
        if (path == null) return
        try {
            val file = File(path)
            if (file.exists() && path.startsWith(context.filesDir.absolutePath)) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteReaderCache(context: Context, bookId: Int): Boolean {
        return try {
            val directory = File(context.cacheDir, "reader/$bookId")
            !directory.exists() || directory.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getFileNameFromUri(context: Context, uriString: String?): String {
        if (uriString.isNullOrBlank()) return "Ningún archivo seleccionado"

        val uri = uriString.toUri()
        var result: String? = null

        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }

        if (result == null) {
            result = uri.path?.substringAfterLast('/')
        }

        return result ?: "Archivo desconocido"
    }

    fun promotePendingFiles(context: Context, epubCandidate: String?, coverCandidate: String?): PromotedBookFiles {
        var epubPath: String? = null
        var coverPath: String? = null
        try {
            if (epubCandidate != null) {
                epubPath = if (epubCandidate.startsWith(context.filesDir.absolutePath)) epubCandidate else saveEpubBookToInternalStorage(context, epubCandidate.toUri())
            }
            if (coverCandidate != null) {
                coverPath = when {
                    coverCandidate.startsWith(context.filesDir.absolutePath) -> coverCandidate
                    coverCandidate.startsWith(context.cacheDir.absolutePath) ->
                        copyCacheFileToFilesDir(
                            context,
                            File(coverCandidate),
                            "cover_${System.currentTimeMillis()}.jpg",
                            R.string.error_copy_cover,
                        )
                    else -> saveImageToInternalStorage(context, coverCandidate.toUri())
                }
            }
            return PromotedBookFiles(epubPath, coverPath)
        } catch (e: JuguitoException) {
            if (epubPath != null) deleteFileFromInternalStorage(context, epubPath)
            if (coverPath != null) deleteFileFromInternalStorage(context, coverPath)
            throw e
        }
    }

    fun deleteStagingAsset(context: Context, path: String?) {
        if (path.isNullOrBlank()) return
        if (!path.startsWith(context.cacheDir.absolutePath)) return
        runCatching { File(path).delete() }
    }

    private fun copyCacheFileToFilesDir(
        context: Context,
        source: File,
        fileName: String,
        @StringRes errorRes: Int,
    ): String {
        if (!source.isFile || !source.absolutePath.startsWith(context.cacheDir.absolutePath)) {
            throw JuguitoException(errorRes)
        }
        return try {
            val dest = File(context.filesDir, fileName)
            source.inputStream().use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            }
            dest.absolutePath
        } catch (e: JuguitoException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw JuguitoException(errorRes)
        }
    }
}
