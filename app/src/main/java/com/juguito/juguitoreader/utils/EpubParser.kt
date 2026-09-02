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

/** Maximum number of bytes extracted for an EPUB cover. */
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

/** Metadata read from an EPUB package during import. */
data class EpubMetaData(
    val title: String?,
    val author: String?,
    val series: String?,
    val seriesOrder: Double?,
    val genres: List<String>,
    val publisher: String?,
    val coverUrl: String?
)

/** Spine order and NCX path produced by parsing an OPF package document. */
internal data class OpfParseResult(
    val spine: List<String>,
    val tocPath: String,
)

/** Parses EPUB metadata and reading content from ZIP-based EPUB files. */
object EpubParser {
    private const val BUFFER_SIZE = 8192

    /**
     * Reads package metadata and extracts a bounded cover image from [uri].
     *
     * Malformed or unavailable optional fields are omitted instead of failing the import.
     */
    fun extractMetadata(context: Context, uri: Uri, persistCover: Boolean): EpubMetaData {
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
                                coverFile = if (persistCover) {
                                    File(context.filesDir, "cover_${System.currentTimeMillis()}.jpg")
                                } else {
                                    File(context.cacheDir, "covers/cover_${System.currentTimeMillis()}.jpg").also { it.parentFile?.mkdirs() }
                                }
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

    /**
     * Extracts and parses the EPUB at [filePath] into the cache directory for [bookId].
     *
     * Reuses an existing extraction, validates every XML-provided file reference against
     * the extraction root, and generates a basic chapter index when the NCX cannot provide one.
     *
     * @throws SecurityException if an archive entry or XML reference escapes the extraction root.
     * @throws IOException if extraction fails or no readable spine can be found.
     */
    fun extractFullContent(context: Context, bookId: Int, filePath: String): EpubContent {
        val directory = File(context.cacheDir, "reader/$bookId")

        ensureExtracted(context, filePath, directory)

        val opfPath: String = parseContainerOpfPath(directory)
        if (opfPath.isEmpty()) throw IOException("Fichero EPUB corrupto.")

        val opf = parseOpf(directory, opfPath)
        if (opf.spine.isEmpty()) throw IOException("Fichero EPUB corrupto.")

        var chaptersTree = parseNcx(directory, opf.tocPath)
        var isIndexAutoGenerated = false

        if (chaptersTree.isEmpty() && opf.spine.isNotEmpty()) {
            isIndexAutoGenerated = true
            chaptersTree = opf.spine.mapIndexed { i, href ->
                EpubNavElement("Capítulo ${i + 1}", href)
            }
        }

        return EpubContent(
            baseDir = directory.path,
            spine = opf.spine,
            chaptersTree = chaptersTree,
            isIndexAutoGenerated = isIndexAutoGenerated
        )
    }

    /**
     * Ensures [directory] contains an extracted EPUB copy of [filePath].
     *
     * Reuses an existing extraction when [directory] already exists. On any failure after
     * creating the directory, removes it so a later attempt can retry cleanly.
     *
     * @throws IOException if the EPUB file cannot be opened or extraction fails.
     * @throws SecurityException if [unzip] rejects the archive.
     */
    private fun ensureExtracted(context: Context, filePath: String, directory: File) {
        if (directory.exists()) return
        directory.mkdirs()
        try {
            val stream = context.contentResolver.openInputStream(Uri.fromFile(File(filePath)))
                ?: throw IOException("No se pudo abrir el EPUB.")
            stream.use { unzip(it, directory.path) }
        } catch (e: Exception) {
            directory.deleteRecursively()
            throw e
        }
    }

    /**
     * Reads `META-INF/container.xml` and returns the OPF path declared by the rootfile entry.
     *
     * @return OPF relative path, or an empty string when the container file is missing or has no rootfile.
     */
    internal fun parseContainerOpfPath(directory: File): String {
        var opfPath = ""
        val containerFile = resolveEpubFile(directory, "META-INF/container.xml")
        if (!containerFile.exists()) return ""

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
        return opfPath
    }

    /**
     * Parses the OPF at [opfPath] and builds the reading spine plus optional NCX path.
     *
     * Every manifest href is validated against [directory] via [resolveEpubFile].
     *
     * @return Empty spine and toc when the OPF file does not exist.
     * @throws SecurityException if a manifest reference escapes [directory].
     */
    internal fun parseOpf(directory: File, opfPath: String): OpfParseResult {
        val opfFile = resolveEpubFile(directory, opfPath)
        if (!opfFile.exists()) return OpfParseResult(emptyList(), "")

        val manifestMap = mutableMapOf<String, String>()
        val spine = mutableListOf<String>()
        var tocPath = ""
        val opfDir = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") + "/" else ""

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
                            val mediaType =
                                parser.getAttributeValue(null, "media-type") ?: ""
                            val resourcePath = opfDir + href
                            val filePath = filePathFromReference(resourcePath)
                            resolveEpubFile(directory, filePath)
                            if (mediaType.contains("ncx")) tocPath = resourcePath
                            manifestMap[id] = resourcePath
                        }

                        "itemref" -> {
                            val idRef = parser.getAttributeValue(null, "idref")
                            manifestMap[idRef]?.let { spine.add(it) }
                        }
                    }
                }
                eventType = parser.next()
            }
        }
        return OpfParseResult(spine, tocPath)
    }

    /**
     * Parses the NCX navigation document at [tocPath], when present.
     *
     * NCX is optional for reading: any parse failure returns an empty list so the caller can
     * fall back to a spine-generated chapter index.
     *
     * @return Parsed chapter tree, or an empty list when [tocPath] is blank or NCX cannot be read.
     */
    internal fun parseNcx(directory: File, tocPath: String): List<EpubNavElement> {
        if (tocPath.isEmpty()) return emptyList()
        return try {
            val chaptersTree = mutableListOf<EpubNavElement>()
            val ncxFile = resolveEpubFile(directory, filePathFromReference(tocPath))
            if (!ncxFile.exists()) throw IOException("Fichero EPUB corrupto.")
            val tocDir = if (tocPath.contains("/")) tocPath.substringBeforeLast("/") + "/" else ""

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
                            chaptersTree.add(parseNavPoint(parser, tocDir, directory))
                        }
                    } else if (eventType == XmlPullParser.END_TAG && parser.name == "navMap") {
                        break
                    }
                    eventType = parser.next()
                }
            }
            filterValidNavElements(chaptersTree)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Drops NCX nodes that carry no navigable target (no [EpubNavElement.href] and no valid children).
     *
     * Malformed or truncated NCX can yield empty nav points without throwing; filtering ensures the
     * caller falls back to a spine-generated index instead of showing a broken table of contents.
     */
    private fun filterValidNavElements(elements: List<EpubNavElement>): List<EpubNavElement> =
        elements.mapNotNull { element ->
            val validChildren = filterValidNavElements(element.children)
            when {
                element.href.isNotBlank() -> element.copy(children = validChildren)
                validChildren.isNotEmpty() -> element.copy(children = validChildren)
                else -> null
            }
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

    /**
     * Extracts [inputStream] into [outputPath] while enforcing [limits].
     *
     * Rejects Zip Slip paths and removes the complete output directory after any failure.
     */
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
            throw SecurityException(e.message)
        }
    }

    /**
     * Parses one NCX nav point recursively and validates its content reference.
     *
     * [tocDir] is relative to the EPUB extraction [directory].
     */
    private fun parseNavPoint(parser: XmlPullParser, tocDir: String, directory: File): EpubNavElement {
        var title = ""
        var href = ""
        val children = mutableListOf<EpubNavElement>()
        var eventType = parser.next()

        while (!(eventType == XmlPullParser.END_TAG && parser.name == "navPoint")) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "text" -> title = parser.nextText()
                    "content" -> {
                        val src = parser.getAttributeValue(null, "src") ?: ""
                        val resourcePath = tocDir + src
                        val filePath = filePathFromReference(resourcePath)
                        resolveEpubFile(directory, filePath)
                        href = resourcePath
                    }

                    "navPoint" -> children.add(parseNavPoint(parser, tocDir, directory))
                }
            }
            if (eventType == XmlPullParser.END_DOCUMENT) break
            eventType = parser.next()
        }

        return EpubNavElement(title, href, children)
    }

    /** Returns the filesystem part of an EPUB resource reference. */
    internal fun filePathFromReference(reference: String): String =
        reference.substringBefore('#').substringBefore('?')

    /**
     * Resolves [relativePath] and ensures its canonical location remains below [root].
     *
     * @throws SecurityException if the resolved path points outside [root].
     */
    internal fun resolveEpubFile(root: File, relativePath: String): File {
        if (File(relativePath).isAbsolute) {
            throw SecurityException("Archivo malicioso detectado. Ruta absoluta no permitida.")
        }

        val newFile = File(root, relativePath)
        val canonicalDirPath = root.canonicalPath
        val canonicalFilePath = newFile.canonicalPath

        if (!canonicalFilePath.startsWith(canonicalDirPath + File.separator)) {
            throw SecurityException("Archivo malicioso detectado. Intentando escapar de los ficheros del EPUB.")
        }
        return newFile.canonicalFile
    }
}
