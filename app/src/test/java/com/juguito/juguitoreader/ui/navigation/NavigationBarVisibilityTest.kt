package com.juguito.juguitoreader.ui.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NavigationBarVisibilityTest {

    @Test
    fun `navigation bar shows on top-level destinations`() {
        assertThat(shouldShowNavigationBar("home")).isTrue()
        assertThat(shouldShowNavigationBar("registry")).isTrue()
        assertThat(shouldShowNavigationBar("library")).isTrue()
        assertThat(shouldShowNavigationBar("library?favourites={favourites}&status={status}")).isTrue()
        assertThat(shouldShowNavigationBar("library?favourites=true&status=READING")).isTrue()
    }

    @Test
    fun `navigation bar hides on secondary routes`() {
        assertThat(shouldShowNavigationBar("management")).isFalse()
        assertThat(shouldShowNavigationBar("settings")).isFalse()
        assertThat(shouldShowNavigationBar("about")).isFalse()
        assertThat(shouldShowNavigationBar("changelog")).isFalse()
        assertThat(shouldShowNavigationBar("add_book")).isFalse()
        assertThat(shouldShowNavigationBar("add_folder")).isFalse()
        assertThat(shouldShowNavigationBar("edit_folder/{folderId}")).isFalse()
        assertThat(shouldShowNavigationBar("book_detail/{bookId}")).isFalse()
        assertThat(shouldShowNavigationBar("reader/{bookId}")).isFalse()
        assertThat(shouldShowNavigationBar(null)).isFalse()
    }
}
