package com.juguito.juguitoreader.domain.changelog

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChangelogVersionsTest {

    @Test
    fun `compareVersionNames treats equal versions as zero`() {
        assertThat(compareVersionNames("1.2.0", "1.2.0")).isEqualTo(0)
        assertThat(compareVersionNames("1.2", "1.2.0")).isEqualTo(0)
    }

    @Test
    fun `compareVersionNames returns positive when first is greater`() {
        assertThat(compareVersionNames("1.2.1", "1.2.0")).isGreaterThan(0)
        assertThat(compareVersionNames("1.10.0", "1.2.0")).isGreaterThan(0)
        assertThat(compareVersionNames("2.0.0", "1.9.9")).isGreaterThan(0)
    }

    @Test
    fun `compareVersionNames returns negative when first is smaller`() {
        assertThat(compareVersionNames("1.2.0", "1.2.1")).isLessThan(0)
    }

    @Test
    fun `unseen with null lastSeen returns only current if it is in catalog`() {
        val catalog = listOf("1.2.1", "1.2.0")

        assertThat(unseenChangelogVersions("1.2.1", lastSeen = null, catalog))
            .containsExactly("1.2.1")
        assertThat(unseenChangelogVersions("1.3.0", lastSeen = null, catalog))
            .isEmpty()
    }

    @Test
    fun `unseen with lastSeen equal to current returns empty`() {
        val catalog = listOf("1.2.1", "1.2.0")

        assertThat(unseenChangelogVersions("1.2.1", lastSeen = "1.2.1", catalog))
            .isEmpty()
    }

    @Test
    fun `unseen from 1_2_0 to 1_2_1 returns only 1_2_1`() {
        val catalog = listOf("1.2.1", "1.2.0")

        assertThat(unseenChangelogVersions("1.2.1", lastSeen = "1.2.0", catalog))
            .containsExactly("1.2.1")
            .inOrder()
    }

    @Test
    fun `unseen skipped version returns all in between newest first`() {
        val catalog = listOf("1.3.0", "1.2.1", "1.2.0")

        assertThat(unseenChangelogVersions("1.3.0", lastSeen = "1.2.0", catalog))
            .containsExactly("1.3.0", "1.2.1")
            .inOrder()
    }

    @Test
    fun `unseen ignores catalog versions newer than current`() {
        val catalog = listOf("1.3.0", "1.2.1", "1.2.0")

        assertThat(unseenChangelogVersions("1.2.1", lastSeen = "1.2.0", catalog))
            .containsExactly("1.2.1")
    }

    @Test
    fun `unseen lastSeen not in catalog still returns versions after it`() {
        val catalog = listOf("1.2.1", "1.2.0")

        assertThat(unseenChangelogVersions("1.2.1", lastSeen = "1.1.0", catalog))
            .containsExactly("1.2.1", "1.2.0")
            .inOrder()
    }

    @Test
    fun `unseen downgrade returns empty`() {
        val catalog = listOf("1.2.1", "1.2.0")

        assertThat(unseenChangelogVersions("1.2.0", lastSeen = "1.2.1", catalog))
            .isEmpty()
    }
}
