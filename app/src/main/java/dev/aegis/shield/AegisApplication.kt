package dev.aegis.shield

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class AegisApplication : Application() {

    companion object {
        const val CHANNEL_ID_PERSISTENCE = "aegis_shield_persistent"
        const val CHANNEL_ID_ALERTS = "aegis_shield_alerts"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val persistentChannel = NotificationChannel(
                CHANNEL_ID_PERSISTENCE,
                "Shield Protection Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the Aegis accessibility engine running in the background"
                setShowBadge(false)
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Dismissal Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows real-time alerts when background popup overlays are dismissed"
            }

            manager?.createNotificationChannel(persistentChannel)
            manager?.createNotificationChannel(alertsChannel)
        }
    }
}
