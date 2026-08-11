package com.juguito.juguitoreader.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

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
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBookToInternalStorage(context: Context, uri: Uri, extension: String = "epub"): String? {
        if (uri.path?.startsWith(context.filesDir.absolutePath) == true) {
            return uri.path
        }

        return try {
            val fileName = "book_${System.currentTimeMillis()}.$extension"
            val file = File(context.filesDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
}
