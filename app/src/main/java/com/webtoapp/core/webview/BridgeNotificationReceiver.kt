package com.webtoapp.core.webview

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import java.util.Locale

class BridgeNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NativeBridge.BRIDGE_NOTIFICATION_ACTION) return

        try {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

            val title = intent.getStringExtra(NativeBridge.EXTRA_BRIDGE_TITLE)
                ?.takeIf { it.isNotBlank() }
                ?: context.applicationInfo.loadLabel(context.packageManager).toString()
            val body = intent.getStringExtra(NativeBridge.EXTRA_BRIDGE_BODY).orEmpty()
            val tag = intent.getStringExtra(NativeBridge.EXTRA_BRIDGE_TAG)
                ?.takeIf { it.isNotBlank() }
                ?: "bridge_${System.currentTimeMillis()}"
            val channelId = intent.getStringExtra(NativeBridge.EXTRA_BRIDGE_CHANNEL_ID)
                ?.takeIf { it.isNotBlank() }
                ?: "webapp_notifications"
            val channelName = intent.getStringExtra(NativeBridge.EXTRA_BRIDGE_CHANNEL_NAME)
                ?.takeIf { it.isNotBlank() }
                ?: Strings.webAppNotificationChannelName
            val channelDesc = intent.getStringExtra(NativeBridge.EXTRA_BRIDGE_CHANNEL_DESC)
                ?: "WebToApp bridge notifications"
            val importanceRaw = intent.getStringExtra(NativeBridge.EXTRA_BRIDGE_IMPORTANCE).orEmpty()
            val enableVibration = intent.getBooleanExtra(NativeBridge.EXTRA_BRIDGE_VIBRATE, true)
            val playSound = intent.getBooleanExtra(NativeBridge.EXTRA_BRIDGE_SOUND, true)
            val deepLink = intent.getStringExtra(NativeBridge.EXTRA_BRIDGE_DEEP_LINK)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val importance = when (importanceRaw.trim().lowercase(Locale.ROOT)) {
                    "max" -> NotificationManager.IMPORTANCE_MAX
                    "high", "urgent" -> NotificationManager.IMPORTANCE_HIGH
                    "low" -> NotificationManager.IMPORTANCE_LOW
                    "min" -> NotificationManager.IMPORTANCE_MIN
                    "none", "silent" -> NotificationManager.IMPORTANCE_NONE
                    else -> NotificationManager.IMPORTANCE_DEFAULT
                }
                val channel = NotificationChannel(channelId, channelName, importance).apply {
                    description = channelDesc
                    enableVibration(enableVibration)
                    setShowBadge(true)
                }
                manager.createNotificationChannel(channel)
            }

            val launchIntent = if (!deepLink.isNullOrBlank()) {
                Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
                    setPackage(context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            } else {
                context.packageManager.getLaunchIntentForPackage(context.packageName)
                    ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP) }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                tag.hashCode(),
                launchIntent
                    ?: Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(context.applicationInfo.icon)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setOngoing(false)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            if (enableVibration) {
                builder.setVibrate(longArrayOf(0L, 250L, 150L, 250L))
            }
            if (!playSound) {
                builder.setSilent(true)
            }

            NotificationManagerCompat.from(context).notify(tag, tag.hashCode(), builder.build())
        } catch (e: Exception) {
            AppLogger.e("BridgeNotificationReceiver", "Failed to post scheduled bridge notification", e)
        }
    }
}

