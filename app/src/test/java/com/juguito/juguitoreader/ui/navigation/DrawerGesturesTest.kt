package com.juguito.juguitoreader.ui.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DrawerGesturesTest {

    @Test
    fun `drawer gestures stay enabled on top-level destinations`() {
        assertThat(areDrawerGesturesEnabled("home")).isTrue()
        assertThat(areDrawerGesturesEnabled("library")).isTrue()
        assertThat(areDrawerGesturesEnabled("registry")).isTrue()
        assertThat(areDrawerGesturesEnabled("management")).isTrue()
        assertThat(areDrawerGesturesEnabled("settings")).isTrue()
        assertThat(areDrawerGesturesEnabled(null)).isTrue()
    }

    @Test
    fun `drawer gestures are locked on add and update routes`() {
        assertThat(areDrawerGesturesEnabled("add_book")).isFalse()
        assertThat(areDrawerGesturesEnabled("add_folder")).isFalse()
        assertThat(areDrawerGesturesEnabled("edit_folder/{folderId}")).isFalse()
        assertThat(areDrawerGesturesEnabled("book_detail/{bookId}")).isFalse()
        assertThat(areDrawerGesturesEnabled("reader/{bookId}")).isFalse()
    }
}
