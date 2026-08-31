package com.juguito.juguitoreader.utils

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FileUtilsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var appFilesDir: File
    private lateinit var appCacheDir: File

    @Before
    fun setup() {
        appFilesDir = tempFolder.newFolder("files")
        appCacheDir = tempFolder.newFolder("cache")
        context = mockk(relaxed = true) {
            every { filesDir } returns appFilesDir
            every { cacheDir } returns appCacheDir
        }
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `promotePendingFiles returns null paths when candidates are null`() {
        val result = FileUtils.promotePendingFiles(context, null, null)

        assertThat(result.epubPath).isNull()
        assertThat(result.coverPath).isNull()
    }

    @Test
    fun `promotePendingFiles keeps paths already under filesDir`() {
        val epub = File(appFilesDir, "book.epub").apply { writeText("epub") }
        val cover = File(appFilesDir, "cover.jpg").apply { writeText("cover") }

        val result = FileUtils.promotePendingFiles(
            context,
            epub.absolutePath,
            cover.absolutePath,
        )

        assertThat(result.epubPath).isEqualTo(epub.absolutePath)
        assertThat(result.coverPath).isEqualTo(cover.absolutePath)
    }

    @Test
    fun `deleteStagingAsset removes cache files only`() {
        val cacheFile = File(appCacheDir, "covers/cover.jpg").apply {
            parentFile?.mkdirs()
            writeText("staging")
        }
        val persistedCover = File(appFilesDir, "cover.jpg").apply { writeText("persisted") }

        FileUtils.deleteStagingAsset(context, cacheFile.absolutePath)
        FileUtils.deleteStagingAsset(context, persistedCover.absolutePath)

        assertThat(cacheFile.exists()).isFalse()
        assertThat(persistedCover.exists()).isTrue()
    }

    @Test
    fun `deleteFileFromInternalStorage removes files under filesDir only`() {
        val internalFile = File(appFilesDir, "book.epub").apply { writeText("epub") }
        val cacheFile = File(appCacheDir, "book.epub").apply { writeText("cache") }

        FileUtils.deleteFileFromInternalStorage(context, internalFile.absolutePath)
        FileUtils.deleteFileFromInternalStorage(context, cacheFile.absolutePath)

        assertThat(internalFile.exists()).isFalse()
        assertThat(cacheFile.exists()).isTrue()
    }
}
