package com.juguito.juguitoreader.utils

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
    fun `unzip should throw SecurityException when entry tries to escape directory`() {
        val maliciousZipFile = tempFolder.newFile("malicious_book.epub")

        ZipOutputStream(FileOutputStream(maliciousZipFile)).use { zos ->
            val badEntry = ZipEntry("../../../malicious_payload.txt")
            zos.putNextEntry(badEntry)
            zos.write("Ataque simulado".toByteArray())
            zos.closeEntry()
        }

        val targetDirectory = tempFolder.newFolder("extracted_epub")
        val entryFile = File(targetDirectory, "../../../malicious_payload.txt")

        assertThrows(SecurityException::class.java) {
            val canonicalDirPath = targetDirectory.canonicalPath
            val canonicalFilePath = entryFile.canonicalPath

            if (!canonicalFilePath.startsWith(canonicalDirPath + File.separator)) {
                throw SecurityException("Archivo malicioso detectado. Intenta escapar del directorio.")
            }
        }
    }
}