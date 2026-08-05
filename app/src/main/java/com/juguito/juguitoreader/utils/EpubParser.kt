package com.juguito.juguitoreader.utils

import android.content.Context
import android.net.Uri
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.util.zip.ZipInputStream

data class EpubMetaData(
    val title: String?,
    val author: String?,
    val series: String?,
    val genres: List<String>,
    val publisher: String?,
    val coverUrl: String?
)

object EpubParser {
    fun extractMetadata(context: Context, uri: Uri): EpubMetaData {
        var title: String? = null
        var author: String? = null
        var series: String? = null
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
            try {
                val opfDir = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") + "/" else ""
                val fullCoverZipPath = opfDir + coverHref

                context.contentResolver.openInputStream(uri).use { inputStream ->
                    ZipInputStream(inputStream).use { zip ->
                        var entry = zip.nextEntry
                        while (entry != null) {
                            if (entry.name == fullCoverZipPath) {
                                val coverFile = File(
                                    context.filesDir,
                                    "cover_${System.currentTimeMillis()}.jpg"
                                )
                                coverFile.outputStream().use { output ->
                                    zip.copyTo(output)
                                }
                                coverUrl = coverFile.absolutePath
                                break
                            }
                            entry = zip.nextEntry
                        }

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return EpubMetaData(title, author, series, genres, publisher, coverUrl)
    }
}