package dev.aegis.shield.automation

import android.annotation.SuppressLint
import android.app.Service
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import dev.aegis.shield.R

/**
 * The persistent foreground notification: channel creation, builder, and the
 * specialUse/dataSync service-type handshake — kept out of the accessibility
 * event loop so a notification hiccup can never delay a dismissal.
 */
object ShieldNotification {

    const val CHANNEL_ID = "gaming_shield_active"
    const val NOTIFICATION_ID = 4213

    /**
     * `specialUse` foreground service with a non-dismissible notification so
     * Android keeps the process alive across long matches. Declared in the
     * manifest with a PROPERTY_SPECIAL_USE_FGS_SUBTYPE justification.
     * Throws to let the caller surface the failure.
     */
    @SuppressLint("InlinedApi") // DATA_SYNC constant inlined on pre-API-29 devices, never executed there
    fun startForeground(service: Service) {
        val manager = NotificationManagerCompat.from(service)
        val channel = NotificationChannelCompat
            .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_MIN)
            .setName(service.getString(R.string.notif_channel_name))
            .setShowBadge(false)
            .build()
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(service, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_shield)
            .setContentTitle(service.getString(R.string.notif_title))
            .setContentText(service.getString(R.string.notif_text))
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        // specialUse is the correct type on Android 14+ (declared + justified
        // in the manifest); older platforms only know dataSync, which is
        // harmless there (no time limits before Android 15).
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        }
        ServiceCompat.startForeground(service, NOTIFICATION_ID, notification, serviceType)
    }
}
