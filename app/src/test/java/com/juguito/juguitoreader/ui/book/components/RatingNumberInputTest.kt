package com.juguito.juguitoreader.ui.book.components

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RatingNumberInputTest {

    @Test
    fun `parseRatingInput empty is zero`() {
        assertThat(parseRatingInput("")).isEqualTo(0f)
        assertThat(parseRatingInput("  ")).isEqualTo(0f)
    }

    @Test
    fun `parseRatingInput accepts comma decimals in range`() {
        assertThat(parseRatingInput("8,5")).isEqualTo(8.5f)
        assertThat(parseRatingInput("10")).isEqualTo(10f)
    }

    @Test
    fun `parseRatingInput rejects out of range`() {
        assertThat(parseRatingInput("11")).isNull()
        assertThat(parseRatingInput("-1")).isNull()
    }

    @Test
    fun `isRatingDraft allows in-progress decimal`() {
        assertThat(isRatingDraft("8.")).isTrue()
        assertThat(isRatingDraft("8,")).isTrue()
        assertThat(isRatingDraft("abc")).isFalse()
    }
}
