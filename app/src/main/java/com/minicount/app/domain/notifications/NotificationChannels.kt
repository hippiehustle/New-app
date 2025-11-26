package com.minicount.app.domain.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages notification channels for the app
 */
@Singleton
class NotificationChannels @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Channel IDs
        const val CHANNEL_EVENT_REMINDERS = "event_reminders"
        const val CHANNEL_EVENT_OCCURRED = "event_occurred"
        const val CHANNEL_WIDGET_UPDATES = "widget_updates"
        const val CHANNEL_GENERAL = "general"

        // Notification IDs
        const val NOTIFICATION_ID_EVENT_REMINDER = 1000
        const val NOTIFICATION_ID_EVENT_OCCURRED = 2000
        const val NOTIFICATION_ID_WIDGET_UPDATE = 3000
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    /**
     * Creates all notification channels for Android O and above
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_EVENT_REMINDERS,
                    "Event Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for upcoming events"
                    enableLights(true)
                    enableVibration(true)
                    setShowBadge(true)
                },

                NotificationChannel(
                    CHANNEL_EVENT_OCCURRED,
                    "Event Occurred",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications when an event date arrives"
                    enableLights(true)
                    setShowBadge(true)
                },

                NotificationChannel(
                    CHANNEL_WIDGET_UPDATES,
                    "Widget Updates",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Background updates for widgets"
                    setShowBadge(false)
                },

                NotificationChannel(
                    CHANNEL_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General app notifications"
                    setShowBadge(true)
                }
            )

            notificationManager.createNotificationChannels(channels)
        }
    }

    /**
     * Get the notification manager
     */
    fun getNotificationManager(): NotificationManager = notificationManager

    /**
     * Check if notifications are enabled for a specific channel
     */
    fun areNotificationsEnabled(channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(channelId)
            return channel?.importance != NotificationManager.IMPORTANCE_NONE
        }
        return notificationManager.areNotificationsEnabled()
    }

    /**
     * Get notification importance for a channel
     */
    fun getChannelImportance(channelId: String): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(channelId)
            return channel?.importance ?: NotificationManager.IMPORTANCE_NONE
        }
        return NotificationManager.IMPORTANCE_DEFAULT
    }

    /**
     * Delete a notification channel
     */
    fun deleteChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(channelId)
        }
    }

    /**
     * Get a base notification builder for a channel
     */
    fun getNotificationBuilder(channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }
}
