package com.juguito.juguitoreader.data.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackupPathsTest {

    private val filesDir = "/data/user/0/com.juguito.juguitoreader/files"

    @Test
    fun `relocate null path returns null`() {
        assertThat(relocateInternalPaths(null, filesDir)).isNull()
    }

    @Test
    fun `relocate blank path returns the same blank`() {
        assertThat(relocateInternalPaths("", filesDir)).isEqualTo("")
        assertThat(relocateInternalPaths("   ", filesDir)).isEqualTo("   ")
    }

    @Test
    fun `relocate absolute android path uses basename under filesDir`() {
        val stored = "/data/user/0/com.juguito.juguitoreader/files/book_1710000000000.epub"

        val relocated = relocateInternalPaths(stored, filesDir)

        assertThat(relocated).isEqualTo("$filesDir/book_1710000000000.epub")
    }

    @Test
    fun `relocate path from another device keeps basename`() {
        val stored = "/data/user/10/com.juguito.juguitoreader/files/cover_1.jpg"

        val relocated = relocateInternalPaths(stored, filesDir)

        assertThat(relocated).isEqualTo("$filesDir/cover_1.jpg")
    }

    @Test
    fun `relocate basename only prefixes filesDir`() {
        assertThat(relocateInternalPaths("cover_1.jpg", filesDir))
            .isEqualTo("$filesDir/cover_1.jpg")
    }

    @Test
    fun `relocate trailing slash returns null`() {
        assertThat(relocateInternalPaths("foo/", filesDir)).isNull()
        assertThat(relocateInternalPaths("/", filesDir)).isNull()
    }
}
