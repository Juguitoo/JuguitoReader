package com.juguito.juguitoreader.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.juguito.juguitoreader.R

fun Context.openDonationPage(): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, getString(R.string.donation_url).toUri())
    return try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}