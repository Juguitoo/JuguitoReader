package com.juguito.juguitoreader.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
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
}
