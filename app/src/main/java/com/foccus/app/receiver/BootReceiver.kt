package com.foccus.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.foccus.app.data.grayscale.GrayscaleManager
import com.foccus.app.data.local.preferences.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferences: UserPreferences

    @Inject
    lateinit var grayscaleManager: GrayscaleManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                preferences.setActiveSessionId(null)
                grayscaleManager.reapplyGrayscaleIfEnabled()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
