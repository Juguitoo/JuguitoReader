package com.juguito.juguitoreader.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class EpubParserExtractTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun parseContainerOpfPath_returns_rootfile_path_from_container_xml() {
        val directory = tempFolder.newFolder("epub")
        writeFile(
            directory,
            "META-INF/container.xml",
            """
            <?xml version="1.0"?>
            <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
              <rootfiles>
                <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
              </rootfiles>
            </container>
            """.trimIndent(),
        )

        val opfPath = EpubParser.parseContainerOpfPath(directory)

        assertThat(opfPath).isEqualTo("OEBPS/content.opf")
    }

    @Test
    fun parseContainerOpfPath_returns_empty_when_container_is_missing() {
        val directory = tempFolder.newFolder("epub_missing_container")

        val opfPath = EpubParser.parseContainerOpfPath(directory)

        assertThat(opfPath).isEmpty()
    }

    @Test
    fun parseOpf_returns_spine_and_ncx_path_from_valid_opf() {
        val directory = tempFolder.newFolder("epub_opf")
        writeFile(
            directory,
            "OEBPS/content.opf",
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <package xmlns="http://www.idpf.org/2007/opf" version="2.0">
              <manifest>
                <item id="ch1" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
                <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
              </manifest>
              <spine toc="ncx">
                <itemref idref="ch1"/>
              </spine>
            </package>
            """.trimIndent(),
        )
        writeFile(directory, "OEBPS/chapter1.xhtml", "<html><body>Chapter</body></html>")

        val opf = EpubParser.parseOpf(directory, "OEBPS/content.opf")

        assertThat(opf.spine).containsExactly("OEBPS/chapter1.xhtml")
        assertThat(opf.tocPath).isEqualTo("OEBPS/toc.ncx")
    }

    @Test
    fun parseOpf_returns_empty_spine_when_opf_file_is_missing() {
        val directory = tempFolder.newFolder("epub_missing_opf")

        val opf = EpubParser.parseOpf(directory, "OEBPS/content.opf")

        assertThat(opf.spine).isEmpty()
        assertThat(opf.tocPath).isEmpty()
    }

    @Test
    fun parseNcx_returns_empty_list_when_toc_path_is_blank() {
        val directory = tempFolder.newFolder("epub_no_toc")

        val chapters = EpubParser.parseNcx(directory, "")

        assertThat(chapters).isEmpty()
    }

    @Test
    fun parseNcx_returns_chapter_tree_from_valid_ncx() {
        val directory = tempFolder.newFolder("epub_ncx")
        writeFile(
            directory,
            "OEBPS/toc.ncx",
            """
            <?xml version="1.0"?>
            <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/">
              <navMap>
                <navPoint>
                  <navLabel><text>Chapter 1</text></navLabel>
                  <content src="chapter1.xhtml"/>
                </navPoint>
              </navMap>
            </ncx>
            """.trimIndent(),
        )
        writeFile(directory, "OEBPS/chapter1.xhtml", "<html><body>Chapter</body></html>")

        val chapters = EpubParser.parseNcx(directory, "OEBPS/toc.ncx")

        assertThat(chapters).hasSize(1)
        assertThat(chapters[0].title).isEqualTo("Chapter 1")
        assertThat(chapters[0].href).isEqualTo("OEBPS/chapter1.xhtml")
    }

    @Test
    fun parseNcx_returns_empty_list_when_ncx_xml_is_invalid() {
        val directory = tempFolder.newFolder("epub_bad_ncx")
        writeFile(directory, "OEBPS/toc.ncx", "<ncx><navMap><navPoint>")

        val chapters = EpubParser.parseNcx(directory, "OEBPS/toc.ncx")

        assertThat(chapters).isEmpty()
    }

    @Test
    fun parseNcx_returns_empty_list_when_ncx_file_is_missing() {
        val directory = tempFolder.newFolder("epub_missing_ncx")

        val chapters = EpubParser.parseNcx(directory, "OEBPS/toc.ncx")

        assertThat(chapters).isEmpty()
    }

    @Test
    fun unzip_and_parse_flow_builds_readable_spine_without_ncx_file() {
        val directory = tempFolder.newFolder("epub_flow")
        val zipFile = tempFolder.newFile("book.epub")
        zipFile.outputStream().use { output ->
            java.util.zip.ZipOutputStream(output).use { zip ->
                zip.putNextEntry(java.util.zip.ZipEntry("META-INF/container.xml"))
                zip.write(
                    """
                    <?xml version="1.0"?>
                    <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                      <rootfiles>
                        <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
                      </rootfiles>
                    </container>
                    """.trimIndent().toByteArray(),
                )
                zip.closeEntry()

                zip.putNextEntry(java.util.zip.ZipEntry("OEBPS/content.opf"))
                zip.write(
                    """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <package xmlns="http://www.idpf.org/2007/opf" version="2.0">
                      <manifest>
                        <item id="ch1" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
                      </manifest>
                      <spine>
                        <itemref idref="ch1"/>
                      </spine>
                    </package>
                    """.trimIndent().toByteArray(),
                )
                zip.closeEntry()

                zip.putNextEntry(java.util.zip.ZipEntry("OEBPS/chapter1.xhtml"))
                zip.write("<html><body>Chapter</body></html>".toByteArray())
                zip.closeEntry()
            }
        }

        val outputDir = File(directory, "extracted")
        zipFile.inputStream().use { input ->
            EpubParser.unzip(input, outputDir.path)
        }

        val opfPath = EpubParser.parseContainerOpfPath(outputDir)
        val opf = EpubParser.parseOpf(outputDir, opfPath)
        val chapters = EpubParser.parseNcx(outputDir, opf.tocPath)

        assertThat(opfPath).isEqualTo("OEBPS/content.opf")
        assertThat(opf.spine).containsExactly("OEBPS/chapter1.xhtml")
        assertThat(chapters).isEmpty()
    }

    private fun writeFile(root: File, relativePath: String, content: String) {
        val file = File(root, relativePath)
        file.parentFile?.mkdirs()
        file.writeText(content)
    }
}
