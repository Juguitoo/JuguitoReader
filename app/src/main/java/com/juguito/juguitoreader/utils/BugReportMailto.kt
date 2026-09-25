package com.juguito.juguitoreader.utils

import java.net.URLEncoder

internal fun bugReportMailto(email: String, subject: String, body: String): String {
    val encodedSubject = URLEncoder.encode(subject, "UTF-8").replace("+", "%20")
    val encodedBody = URLEncoder.encode(body, "UTF-8").replace("+", "%20")
    return "mailto:$email?subject=$encodedSubject&body=$encodedBody"
}
