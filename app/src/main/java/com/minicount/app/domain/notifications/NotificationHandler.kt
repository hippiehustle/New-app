package com.minicount.app.domain.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.minicount.app.R
import com.minicount.app.data.local.entity.Event
import com.minicount.app.domain.util.CountdownCalculator
import com.minicount.app.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles creation and display of notifications
 */
@Singleton
class NotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationChannels: NotificationChannels
) {
    companion object {
        private const val REQUEST_CODE_OPEN_APP = 1001
        private const val REQUEST_CODE_OPEN_EVENT = 1002
    }

    /**
     * Send a reminder notification for an upcoming event
     */
    fun sendEventReminder(event: Event, daysUntil: Int) {
        val countdown = CountdownCalculator.calculate(event.targetDate)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("eventId", event.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_EVENT + event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = notificationChannels.getNotificationBuilder(NotificationChannels.CHANNEL_EVENT_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${event.category.icon} ${event.title}")
            .setContentText("Only $daysUntil days left! ${countdown.days} days, ${countdown.hours} hours")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(buildReminderMessage(event, countdown))
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(
            NotificationChannels.NOTIFICATION_ID_EVENT_REMINDER + event.id.toInt(),
            notification
        )
    }

    /**
     * Send a notification when an event occurs
     */
    fun sendEventOccurred(event: Event) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("eventId", event.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_EVENT + event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = notificationChannels.getNotificationBuilder(NotificationChannels.CHANNEL_EVENT_OCCURRED)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${event.category.icon} ${event.title}")
            .setContentText("Today is the day!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${event.title} is happening today! ${event.category.displayName} on ${event.targetDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}")
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(
            NotificationChannels.NOTIFICATION_ID_EVENT_OCCURRED + event.id.toInt(),
            notification
        )
    }

    /**
     * Send a simple notification with title and message
     */
    fun sendSimpleNotification(
        title: String,
        message: String,
        channelId: String = NotificationChannels.CHANNEL_GENERAL,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = notificationChannels.getNotificationBuilder(channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * Send a notification with a large image
     */
    fun sendBigPictureNotification(
        title: String,
        message: String,
        bitmap: Bitmap,
        event: Event? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            event?.let { putExtra("eventId", it.id) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            event?.id?.toInt() ?: REQUEST_CODE_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = notificationChannels.getNotificationBuilder(NotificationChannels.CHANNEL_EVENT_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null as Bitmap?)
            )
            .setLargeIcon(bitmap)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationId = event?.let {
            NotificationChannels.NOTIFICATION_ID_EVENT_REMINDER + it.id.toInt()
        } ?: System.currentTimeMillis().toInt()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * Cancel a notification
     */
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }

    /**
     * Cancel notifications for a specific event
     */
    fun cancelEventNotifications(eventId: Long) {
        val notificationIds = listOf(
            NotificationChannels.NOTIFICATION_ID_EVENT_REMINDER + eventId.toInt(),
            NotificationChannels.NOTIFICATION_ID_EVENT_OCCURRED + eventId.toInt()
        )

        notificationIds.forEach { cancelNotification(it) }
    }

    /**
     * Build a formatted reminder message
     */
    private fun buildReminderMessage(event: Event, countdown: CountdownCalculator.CountdownData): String {
        return buildString {
            append("${event.title} is coming up!\n")
            append("${event.category.displayName} on ${event.targetDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))}\n\n")
            append("Time remaining:\n")
            if (countdown.days > 0) append("${countdown.days} days ")
            if (countdown.hours > 0) append("${countdown.hours} hours ")
            if (countdown.minutes > 0) append("${countdown.minutes} minutes")

            if (event.notes.isNotEmpty()) {
                append("\n\nNotes: ${event.notes}")
            }
        }
    }

    /**
     * Check if notifications are enabled
     */
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}
