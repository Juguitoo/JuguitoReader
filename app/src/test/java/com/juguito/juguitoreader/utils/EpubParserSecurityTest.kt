package com.juguito.juguitoreader.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class EpubParserSecurityTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `unzip throws SecurityException when entry tries to escape directory`() {
        val zipFile = writeZip("zip_slip.epub") {
            putEntry("../../../malicious_payload.txt", "payload")
        }
        val outputDir = tempFolder.newFolder("out_slip")

        assertThrows(SecurityException::class.java) {
            unzip(zipFile, outputDir)
        }

        assertThat(outputDir.exists()).isFalse()
        assertThat(File(tempFolder.root, "malicious_payload.txt").exists()).isFalse()
    }

    @Test
    fun `unzip throws when a single entry exceeds maxEntryUncompressedBytes`() {
        val zipFile = writeZip("huge_entry.epub") {
            putEntry("chapter.xhtml", ByteArray(200) { 'a'.code.toByte() })
        }
        val outputDir = tempFolder.newFolder("out_entry")
        val limits = ZipExtractionLimits(maxEntryUncompressedBytes = 100L)

        assertThrows(SecurityException::class.java) {
            unzip(zipFile, outputDir, limits)
        }

        assertThat(outputDir.exists()).isFalse()
    }

    @Test
    fun `unzip throws when total uncompressed size exceeds maxUncompressedBytes`() {
        val zipFile = writeZip("huge_total.epub") {
            putEntry("a.txt", ByteArray(80) { 'a'.code.toByte() })
            putEntry("b.txt", ByteArray(80) { 'b'.code.toByte() })
        }
        val outputDir = tempFolder.newFolder("out_total")
        val limits = ZipExtractionLimits(
            maxUncompressedBytes = 100L,
            maxEntryUncompressedBytes = 1_000L,
        )

        assertThrows(SecurityException::class.java) {
            unzip(zipFile, outputDir, limits)
        }

        assertThat(outputDir.exists()).isFalse()
    }

    @Test
    fun `unzip throws when entry count exceeds maxZipEntries`() {
        val zipFile = writeZip("many_entries.epub") {
            repeat(3) { index ->
                putEntry("file_$index.txt", "x")
            }
        }
        val outputDir = tempFolder.newFolder("out_count")
        val limits = ZipExtractionLimits(maxZipEntries = 2)

        assertThrows(SecurityException::class.java) {
            unzip(zipFile, outputDir, limits)
        }

        assertThat(outputDir.exists()).isFalse()
    }

    @Test
    fun `unzip extracts valid archive under limits`() {
        val zipFile = writeZip("valid.epub") {
            putEntry("META-INF/container.xml", "<container/>")
            putEntry("OEBPS/ch1.xhtml", "<html/>")
        }
        val outputDir = tempFolder.newFolder("out_ok")

        unzip(zipFile, outputDir)

        assertThat(File(outputDir, "META-INF/container.xml").readText()).isEqualTo("<container/>")
        assertThat(File(outputDir, "OEBPS/ch1.xhtml").readText()).isEqualTo("<html/>")
    }

    @Test
    fun `writeBounded throws and leaves no usable cover when size exceeds max`() {
        val coverFile = tempFolder.newFile("cover_oversize.jpg")
        val payload = ByteArray(200) { 0xFF.toByte() }

        assertThrows(SecurityException::class.java) {
            payload.inputStream().use { input ->
                EpubParser.writeBounded(input, coverFile, maxBytes = 100L)
            }
        }

        coverFile.delete()
        assertThat(coverFile.exists()).isFalse()
    }

    @Test
    fun `writeBounded writes cover when under maxBytes`() {
        val coverFile = tempFolder.newFile("cover_ok.jpg")
        val payload = ByteArray(50) { 0xAB.toByte() }

        payload.inputStream().use { input ->
            EpubParser.writeBounded(input, coverFile, maxBytes = 100L)
        }

        assertThat(coverFile.exists()).isTrue()
        assertThat(coverFile.length()).isEqualTo(50L)
        assertThat(coverFile.readBytes()).isEqualTo(payload)
    }

    private fun unzip(
        zipFile: File,
        outputDir: File,
        limits: ZipExtractionLimits = ZipExtractionLimits(),
    ) {
        zipFile.inputStream().use { input ->
            EpubParser.unzip(input, outputDir.path, limits)
        }
    }

    private fun writeZip(name: String, block: ZipOutputStream.() -> Unit): File {
        val zipFile = tempFolder.newFile(name)
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            zos.block()
        }
        return zipFile
    }

    private fun ZipOutputStream.putEntry(name: String, content: String) {
        putEntry(name, content.toByteArray())
    }

    private fun ZipOutputStream.putEntry(name: String, content: ByteArray) {
        putNextEntry(ZipEntry(name))
        write(content)
        closeEntry()
    }
}
