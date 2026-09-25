package com.juguito.juguitoreader.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BugReportMailtoTest {

    @Test
    fun `mailto keeps the address and encodes subject and body`() {
        val uri = bugReportMailto(
            email = "h.juagom@proton.me",
            subject = "JuguitoReader — informe",
            body = "Versión: 1.2.2\nQué ha pasado:\n"
        )

        assertThat(uri).startsWith("mailto:h.juagom@proton.me?")
        assertThat(uri).contains("subject=JuguitoReader%20%E2%80%94%20informe")
        assertThat(uri).contains("body=Versi%C3%B3n%3A%201.2.2%0AQu%C3%A9%20ha%20pasado%3A%0A")
    }
}
