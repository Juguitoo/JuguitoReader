package com.juguito.epubengine

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EpubEngineTest {

    @Test
    fun `scaffold type is loadable`() {
        assertThat(EpubEngine::class.java.name).isEqualTo("com.juguito.epubengine.EpubEngine")
    }
}
