package com.juguito.juguitoreader.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.net.toUri
import com.juguito.juguitoreader.R

fun Context.openBugReport(): Boolean {
    val version = appVersionName().ifBlank { "—" }
    val device = listOf(Build.MANUFACTURER, Build.MODEL)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "—" }
    val androidVersion = Build.VERSION.RELEASE?.ifBlank { null } ?: "—"
    val subject = getString(R.string.bug_report_email_subject)
    val body = getString(R.string.bug_report_email_body, version, device, androidVersion)
    val intent = Intent(Intent.ACTION_SENDTO, bugReportMailto(
        email = getString(R.string.bug_report_email),
        subject = subject,
        body = body
    ).toUri())
    return try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
