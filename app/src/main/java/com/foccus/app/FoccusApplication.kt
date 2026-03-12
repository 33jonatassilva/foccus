package com.foccus.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FoccusApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val focusChannel = NotificationChannel(
                FOCUS_SESSION_CHANNEL_ID,
                "Sessão de Foco",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificações durante sessões de foco ativas"
                setShowBadge(false)
            }

            val blockChannel = NotificationChannel(
                BLOCK_ALERT_CHANNEL_ID,
                "Alertas de Bloqueio",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas quando um app bloqueado é acessado"
            }

            notificationManager.createNotificationChannels(listOf(focusChannel, blockChannel))
        }
    }

    companion object {
        const val FOCUS_SESSION_CHANNEL_ID = "focus_session_channel"
        const val BLOCK_ALERT_CHANNEL_ID = "block_alert_channel"
    }
}
