package com.juguito.juguitoreader.ui.settings.backup

import android.content.Context
import android.content.Intent

interface AppRestarter {
    fun restart()
}

class ProcessAppRestarter(context: Context) : AppRestarter {
    private val appContext = context.applicationContext

    override fun restart() {
        val intent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
            ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        appContext.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}
