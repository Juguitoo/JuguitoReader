package com.juguito.juguitoreader

import android.app.Application
import com.juguito.juguitoreader.debug.AppInitializerEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class JuguitoReaderApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val initializers = EntryPointAccessors.fromApplication(
            this,
            AppInitializerEntryPoint::class.java,
        ).initializers()
        initializers.forEach { initializer ->
            appScope.launch { initializer.onAppStart() }
        }
    }
}
