package com.juguito.juguitoreader.utils

import android.content.Context
import android.net.Uri
import android.util.Xml
import com.juguito.juguitoreader.domain.model.EpubContent
import com.juguito.juguitoreader.domain.model.EpubNavElement
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipInputStream

/** Production ceilings for EPUB unzip (uncompressed bytes / entry count). */
internal const val MAX_UNCOMPRESSED_BYTES = 512L * 1024 * 1024
internal const val MAX_ENTRY_UNCOMPRESSED_BYTES = 128L * 1024 * 1024
internal const val MAX_ZIP_ENTRIES = 10_000

internal const val MAX_COVER_BYTES = 20L * 1024 * 1024

/**
 * Limits applied while extracting a ZIP/EPUB.
 * Defaults match production; tests pass tighter values to avoid huge fixtures.
 */
internal data class ZipExtractionLimits(
    val maxUncompressedBytes: Long = MAX_UNCOMPRESSED_BYTES,
    val maxEntryUncompressedBytes: Long = MAX_ENTRY_UNCOMPRESSED_BYTES,
    val maxZipEntries: Int = MAX_ZIP_ENTRIES,
)

data class EpubMetaData(
    val title: String?,
    val author: String?,
    val series: String?,
    val seriesOrder: Double?,
    val genres: List<String>,
    val publisher: String?,
    val coverUrl: String?
)

object EpubParser {
    private const val BUFFER_SIZE = 8192

    fun extractMetadata(context: Context, uri: Uri): EpubMetaData {
        var title: String? = null
        var author: String? = null
        var series: String? = null
        var seriesOrder: Double? = null
        val genres = mutableListOf<String>()
        var publisher: String? = null
        var coverId: String? = null
        var coverHref: String? = null
        var coverUrl: String? = null
        var opfPath = ""

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zip ->
                    var entry = zip.nextEntry

                    while(entry != null) {
                        if (entry.name.endsWith(".opf")) {
                            opfPath = entry.name
                            val parser = Xml.newPullParser()
                            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                            parser.setInput(zip, null)

                            var eventType = parser.eventType

                            while(eventType != XmlPullParser.END_DOCUMENT) {
                                if (eventType == XmlPullParser.START_TAG) {
                                    val tagName = parser.name ?: ""

                                    if (tagName.contains("title", ignoreCase = true) && title == null) {
                                        title = parser.nextText()
                                    } else if (tagName.contains("creator", ignoreCase = true) && author == null) {
                                        author = parser.nextText()
                                    } else if (tagName.contains("subject", ignoreCase = true)) {
                                        val genre = parser.nextText()
                                        if (!genre.isNullOrBlank()) genres.add(genre)
                                    } else if (tagName.equals("meta", ignoreCase = true)) {
                                        val nameAttr = parser.getAttributeValue(null, "name")
                                        val contentAttr = parser.getAttributeValue(null, "content")

                                        if (nameAttr == "calibre:series") {
                                            series = contentAttr
                                        } else if (nameAttr == "calibre:series_index") {
                                            seriesOrder = contentAttr.toDoubleOrNull()
                                        } else if (nameAttr == "cover") {
                                            coverId = contentAttr
                                        }
                                    } else if (tagName.contains("publisher", ignoreCase = true) && publisher == null) {
                                        publisher = parser.nextText()
                                    } else if (tagName.contains("item", ignoreCase = true) && coverId != null) {
                                        val idAttr = parser.getAttributeValue(null, "id")
                                        val hrefAttr = parser.getAttributeValue(null, "href")
                                        if (idAttr == coverId && hrefAttr != null) coverHref = hrefAttr
                                    }
                                }
                                eventType = parser.next()
                            }
                            break
                        }
                        entry = zip.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (coverHref != null) {
            var coverFile: File? = null
            try {
                val opfDir = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") + "/" else ""
                val fullCoverZipPath = opfDir + coverHref
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zip ->
                        var entry = zip.nextEntry
                        while (entry != null) {
                            if (entry.name == fullCoverZipPath) {
                                coverFile = File(context.filesDir, "cover_${System.currentTimeMillis()}.jpg")
                                writeBounded(zip, coverFile, MAX_COVER_BYTES)
                                coverUrl = coverFile.absolutePath
                                break
                            }
                            entry = zip.nextEntry
                        }
                    }
                }
            } catch (e: Exception) {
                coverFile?.delete()
                coverUrl = null
                e.printStackTrace()
            }
        }
        return EpubMetaData(
            title = title,
            author = author,
            series = series,
            seriesOrder = seriesOrder,
            genres = genres,
            publisher = publisher,
            coverUrl = coverUrl
        )
    }

    fun extractFullContent(context: Context, bookId: Int, filePath: String): EpubContent {
        val directory = File(context.cacheDir, "reader/$bookId")
        val manifestMap = mutableMapOf<String, String>()
        val spine = mutableListOf<String>()
        val chaptersTree = mutableListOf<EpubNavElement>()
        var isIndexAutoGenerated = false
        var opfPath = ""
        var tocPath = ""

        try {
            val zipFileUri = Uri.fromFile(File(filePath))
            if (!directory.exists()) {
                directory.mkdirs()
                context.contentResolver.openInputStream(zipFileUri)?.use {
                    unzip(it, directory.path)
                }
            }

            val containerFile = File(directory, "META-INF/container.xml")
            if (containerFile.exists()) {
                containerFile.inputStream().use { inputStream ->
                    val parser = Xml.newPullParser()
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                    parser.setInput(inputStream, null)
                    var eventType = parser.eventType
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && parser.name == "rootfile") {
                            opfPath = parser.getAttributeValue(null, "full-path") ?: ""
                            break
                        }
                        eventType = parser.next()
                    }
                }
            }

            if (opfPath.isNotEmpty()) {
                val opfFile = File(directory, opfPath)
                val opfDir = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") + "/" else ""

                if (opfFile.exists()) {
                    opfFile.inputStream().use { opfStream ->
                        val parser = Xml.newPullParser()
                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                        parser.setInput(opfStream, null)
                        var eventType = parser.eventType
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG) {
                                when (parser.name) {
                                    "item" -> {
                                        val id = parser.getAttributeValue(null, "id") ?: ""
                                        val href = parser.getAttributeValue(null, "href") ?: ""
                                        val mediaType = parser.getAttributeValue(null, "media-type") ?: ""
                                        if (mediaType.contains("ncx")) tocPath = opfDir + href
                                        manifestMap[id] = opfDir + href
                                    }
                                    "itemref" -> {
                                        val idref = parser.getAttributeValue(null, "idref")
                                        manifestMap[idref]?.let { spine.add(it) }
                                    }
                                }
                            }
                            eventType = parser.next()
                        }
                    }
                }

                if (tocPath.isNotEmpty()) {
                    val ncxFile = File(directory, tocPath)
                    val tocDir = if (tocPath.contains("/")) tocPath.substringBeforeLast("/") + "/" else ""
                    if (ncxFile.exists()) {
                        ncxFile.inputStream().use { tocStream ->
                            val parser = Xml.newPullParser()
                            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                            parser.setInput(tocStream, null)
                            var eventType = parser.eventType
                            var navMapOpen = false
                            while (eventType != XmlPullParser.END_DOCUMENT) {
                                if (eventType == XmlPullParser.START_TAG) {
                                    if (parser.name == "navMap") navMapOpen = true
                                    else if (navMapOpen && parser.name == "navPoint") {
                                        chaptersTree.add(parseNavPoint(parser, tocDir))
                                    }
                                } else if (eventType == XmlPullParser.END_TAG && parser.name == "navMap") {
                                    break
                                }
                                eventType = parser.next()
                            }
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            throw e
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            isIndexAutoGenerated = true
        }

        if (chaptersTree.isEmpty() && spine.isNotEmpty()) {
            isIndexAutoGenerated = true
            spine.forEachIndexed { index, href ->
                chaptersTree.add(EpubNavElement(title = "Capítulo ${index + 1}", href = href))
            }
        }

        if (spine.isEmpty()) {
            throw IOException("No se ha podido encontrar el contenido de lectura en el libro. El fichero epub del libro puede estar corrupto, prueba a reemplazarlo por uno distinto.")
        }

        return EpubContent(
            baseDir = directory.path,
            spine = spine,
            chaptersTree = chaptersTree,
            isIndexAutoGenerated = isIndexAutoGenerated
        )
    }

    /**
     * Copies [input] into [output], aborting with [SecurityException] if more than
     * [maxBytes] would be written. Returns bytes actually written.
     */
    internal fun copyBounded(
        input: InputStream,
        output: OutputStream,
        maxBytes: Long,
    ): Long {
        val buffer = ByteArray(BUFFER_SIZE)
        var written = 0L
        var len: Int
        while (input.read(buffer).also { len = it } > 0) {
            written += len
            if (written > maxBytes) {
                throw SecurityException("Posible archivo malicioso, excesivamente grande.")
            }
            output.write(buffer, 0, len)
        }
        return written
    }

    /**
     * Copies [input] into [outputFile] with a byte ceiling.
     * Caller owns cleanup of [outputFile] on failure.
     */
    internal fun writeBounded(
        input: InputStream,
        outputFile: File,
        maxBytes: Long = MAX_COVER_BYTES,
    ) {
        outputFile.outputStream().use { output ->
            copyBounded(input, output, maxBytes)
        }
    }

    internal fun unzip(
        inputStream: InputStream,
        outputPath: String,
        limits: ZipExtractionLimits = ZipExtractionLimits(),
    ) {
        val outputDir = File(outputPath)
        if (outputDir.exists()) outputDir.deleteRecursively()
        outputDir.mkdirs()

        try {
            ZipInputStream(inputStream).use { zipInputStream ->
                var entries = 0
                var totalWritten = 0L

                var entry = zipInputStream.nextEntry

                while (entry != null) {
                    entries++
                    if (entries > limits.maxZipEntries) {
                        throw SecurityException("Posible archivo malicioso, excesivamente grande.")
                    }

                    val newFile = File(outputDir, entry.name)

                    val canonicalDirPath = outputDir.canonicalPath
                    val canonicalFilePath = newFile.canonicalPath

                    if (!canonicalFilePath.startsWith(canonicalDirPath + File.separator)) {
                        throw SecurityException("Archivo malicioso detectado. Intenta escapar del directorio: ${entry.name}")
                    }

                    if (entry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()
                        val remainingTotal = limits.maxUncompressedBytes - totalWritten
                        val cap = minOf(limits.maxEntryUncompressedBytes, remainingTotal)
                        if (cap <= 0L) {
                            throw SecurityException("Posible archivo malicioso, excesivamente grande.")
                        }
                        newFile.outputStream().use { output ->
                            totalWritten += copyBounded(zipInputStream, output, cap)
                        }
                    }
                    entry = zipInputStream.nextEntry
                }
            }
        } catch (e: Exception) {
            outputDir.deleteRecursively()
            throw e
        }
    }

    private fun parseNavPoint(parser: XmlPullParser, tocDir: String): EpubNavElement {
        var title = ""
        var href = ""
        val children = mutableListOf<EpubNavElement>()
        var eventType = parser.next()

        try {
            while (!(eventType == XmlPullParser.END_TAG && parser.name == "navPoint")) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "text" -> title = parser.nextText()
                        "content" -> href = tocDir + (parser.getAttributeValue(null, "src") ?: "")
                        "navPoint" -> children.add(parseNavPoint(parser, tocDir))
                    }
                }
                if (eventType == XmlPullParser.END_DOCUMENT) break
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return EpubNavElement(title, href, children)
    }
}
